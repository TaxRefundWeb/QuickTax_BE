package com.quicktax.demo.ocr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quicktax.demo.domain.cases.draft.CaseDraftYear;
import com.quicktax.demo.domain.ocr.OcrJob;
import com.quicktax.demo.domain.ocr.OcrJobErrorCode;
import com.quicktax.demo.repo.draft.CaseDraftYearRepository;
import com.quicktax.demo.repo.ocr.OcrJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OcrPipelineService {

    private final S3Client s3;
    private final CaseDraftYearRepository caseDraftYearRepository;

    private final ClovaTemplateOcrClient clovaTemplateOcrClient;
    private final OcrTemplateUpsertService upsertService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final com.quicktax.demo.service.s3.S3PresignService presignService;
    private final OcrJobRepository ocrJobRepository;

    /**
     * - year ASC
     * - reductionYn ? 6 : 3 페이지 단위로 연도 PDF 분할하여 S3 inbox/year/{caseYear}.pdf 저장
     * - 3장: 1p,2p만 OCR
     * - 6장: 1p,2p,4p,5p만 OCR (1,4는 템플릿#1 / 2,5는 템플릿#2)
     * - salary(템플릿#1): salary1~3 -> company_id (offset+1..offset+3)로 저장 (null이면 row 생성 X)
     * - detail(템플릿#2): 6장일 때 2p는 apply, 5p는 add(합산)
     * - OCR 원본 응답은 outbox/year/{caseYear}/ocr.json 저장
     * - 전체 성공: READY / 하나라도 실패: FAILED
     *
     * 주의: FAILED 찍고 throw 하면 트랜잭션 롤백되면서 FAILED가 안 남고 PROCESSING 고착될 수 있음.
     *      그래서 여기선 markFailed 후 return 한다.
     */
    @Transactional(rollbackFor = Exception.class)
    public void handle(OcrStartMessage msg) throws Exception {
        Long caseId = msg.getCaseId();

        OcrJob job = ocrJobRepository.findById(caseId)
                .orElseThrow(() -> new IllegalStateException("ocr_job not found: caseId=" + caseId));

        String bucket = presignService.bucket();
        String rawKey = job.getOriginalS3Key();

        if (rawKey == null || rawKey.isBlank()) {
            job.markFailed(OcrJobErrorCode.WORKER_EXCEPTION.name(), "original_s3_key is blank");
            return;
        }

        job.markProcessing();

        String casePrefix;
        try {
            casePrefix = casePrefixFromRawKey(rawKey); // {env}/cases/{caseId}
        } catch (Exception e) {
            job.markFailed(OcrJobErrorCode.WORKER_EXCEPTION.name(), e.getMessage());
            return;
        }

        Path workDir = Files.createTempDirectory("quicktax_ocr_" + caseId + "_");
        Path rawPdf = workDir.resolve("raw.pdf");

        try {
            // 0) 재업로드 대비 정리(S3)
            deletePrefix(bucket, casePrefix + "/inbox/year/");
            deletePrefix(bucket, casePrefix + "/outbox/");

            // 1) raw pdf 다운로드
            s3.getObject(
                    GetObjectRequest.builder().bucket(bucket).key(rawKey).build(),
                    ResponseTransformer.toFile(rawPdf)
            );
            log.info("OCR: downloaded raw pdf caseId={} path={}", caseId, rawPdf);

            // 2) year ASC + expected pages 검증
            List<CaseDraftYear> years = caseDraftYearRepository.findAllByIdCaseIdOrderByIdCaseYearAsc(caseId);
            if (years.isEmpty()) {
                job.markFailed(OcrJobErrorCode.WORKER_EXCEPTION.name(), "case_draft_year empty. caseId=" + caseId);

                return;
            }

            int expectedPages = years.stream()
                    .mapToInt(y -> y.isReductionYn() ? 6 : 3)
                    .sum();

            try (PDDocument rawDoc = Loader.loadPDF(rawPdf.toFile())) {
                int actualPages = rawDoc.getNumberOfPages();

                if (actualPages != expectedPages) {
                    job.markFailed(
                            OcrJobErrorCode.PAGE_RULE_MISMATCH.name(),
                            "expected=" + expectedPages + ", actual=" + actualPages
                    );
                    return;
                }

                // 3) 연도별 분할 + OCR
                int cursor = 0;

                for (CaseDraftYear y : years) {
                    int caseYear = y.getId().getCaseYear();
                    int pagesForYear = y.isReductionYn() ? 6 : 3;

                    // 3-1) year pdf 생성
                    Path yearPdf = workDir.resolve(caseYear + ".pdf");
                    try (PDDocument yearDoc = new PDDocument()) {
                        for (int i = 0; i < pagesForYear; i++) {
                            yearDoc.importPage(rawDoc.getPage(cursor + i));
                        }
                        yearDoc.save(yearPdf.toFile());
                    }

                    // 3-2) inbox/year 업로드
                    String yearPdfKey = casePrefix + "/inbox/year/" + caseYear + ".pdf";
                    putObject(bucket, yearPdfKey, "application/pdf", Files.readAllBytes(yearPdf));

                    // 3-3) DB 리셋(재업로드/재처리 대비)
                    upsertService.resetYear(caseId, caseYear);

                    // 3-4) 필요한 페이지만 1장씩 OCR
                    ArrayNode yearResponses = objectMapper.createArrayNode();

                    try (PDDocument yearDoc = Loader.loadPDF(yearPdf.toFile())) {
                        int[] wantedPages = wantedPagesForYear(pagesForYear);

                        for (int pageNoWithinYear : wantedPages) {
                            int templateIndex = templateIndexForPage(pageNoWithinYear); // 0=salary, 1=detail
                            boolean isSalaryPage = (templateIndex == 0);

                            Path onePage = workDir.resolve(caseYear + "_p" + pageNoWithinYear + ".pdf");
                            try (PDDocument oneDoc = new PDDocument()) {
                                oneDoc.importPage(yearDoc.getPage(pageNoWithinYear - 1));
                                oneDoc.save(onePage.toFile());
                            }

                            JsonNode ocrResp;
                            try {
                                ocrResp = clovaTemplateOcrClient.inferPdf(
                                        onePage,
                                        "case" + caseId + "_" + caseYear + "_p" + pageNoWithinYear,
                                        templateIndex
                                );
                            } catch (Exception e) {
                                job.markFailed(
                                        OcrJobErrorCode.OCR_CALL_FAILED.name(),
                                        "caseYear=" + caseYear + " page=" + pageNoWithinYear + " err=" + e.getMessage()
                                );
                                return;
                            }

                            Map<String, String> fields;
                            try {
                                fields = ClovaTemplateFieldExtractor.extractNameToInferText(ocrResp);
                            } catch (Exception e) {
                                job.markFailed(
                                        OcrJobErrorCode.OCR_PARSE_ERROR.name(),
                                        "caseYear=" + caseYear + " page=" + pageNoWithinYear + " err=" + e.getMessage()
                                );
                                return;
                            }

                            // 3-5) 페이지별 저장 규칙
                            if (isSalaryPage) {
                                int offset = (pageNoWithinYear == 4) ? 3 : 0; // 6장일 때 4p는 회사 4~6
                                upsertService.upsertSalaryPage(caseId, caseYear, fields, offset);
                            } else {
                                // 6장일 때만 5p(add)로 합산. 2p는 apply.
                                boolean add = (pagesForYear == 6 && pageNoWithinYear == 5);
                                upsertService.upsertDetailPage(caseId, caseYear, fields, add);
                            }

                            // 3-6) outbox 저장용 원본 응답 누적
                            ObjectNode wrapper = objectMapper.createObjectNode();
                            wrapper.put("pageNo", pageNoWithinYear);
                            wrapper.put("templateIndex", templateIndex + 1);
                            wrapper.set("response", ocrResp);
                            yearResponses.add(wrapper);
                        }
                    }

                    // 3-7) outbox/year/{year}/ocr.json 저장
                    String ocrJsonKey = casePrefix + "/outbox/year/" + caseYear + "/ocr.json";
                    byte[] jsonBytes = objectMapper.writerWithDefaultPrettyPrinter()
                            .writeValueAsString(yearResponses)
                            .getBytes(StandardCharsets.UTF_8);
                    putObject(bucket, ocrJsonKey, "application/json", jsonBytes);

                    log.info("OCR: done caseId={} caseYear={} pages={} inboxYearPdfKey={} outboxOcrKey={}",
                            caseId, caseYear, pagesForYear, yearPdfKey, ocrJsonKey);

                    cursor += pagesForYear;
                }
            }

            job.markReady();

        } catch (Exception e) {
            job.markFailed(
                    OcrJobErrorCode.WORKER_EXCEPTION.name(),
                    e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage()
            );
            return;

        } finally {
            try (var stream = Files.walk(workDir)) {
                stream.sorted(Comparator.reverseOrder()).forEach(p -> {
                    try { Files.deleteIfExists(p); } catch (Exception ignore) {}
                });
            } catch (Exception ignore) {}
        }
    }

    private static int[] wantedPagesForYear(int pagesForYear) {
        if (pagesForYear == 3) return new int[]{1, 2};
        if (pagesForYear == 6) return new int[]{1, 2, 4, 5};
        throw new IllegalArgumentException("Unsupported pagesForYear=" + pagesForYear);
    }

    /**
     * templateIndex:
     * - 0: salary 템플릿(1,4 page)
     * - 1: detail 템플릿(2,5 page)
     */
    private static int templateIndexForPage(int pageNoWithinYear) {
        return (pageNoWithinYear == 1 || pageNoWithinYear == 4) ? 0 : 1;
    }

    private static String casePrefixFromRawKey(String rawKey) {
        // 예: stg/cases/66/inbox/raw/xxxx.pdf -> stg/cases/66
        int idx = rawKey.indexOf("/inbox/raw/");
        if (idx < 0) throw new IllegalArgumentException("rawS3Key does not contain /inbox/raw/: " + rawKey);
        return rawKey.substring(0, idx);
    }

    private void putObject(String bucket, String key, String contentType, byte[] bytes) {
        s3.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType(contentType)
                        .build(),
                RequestBody.fromBytes(bytes)
        );
    }

    private void deletePrefix(String bucket, String prefix) {
        String token = null;
        while (true) {
            ListObjectsV2Response list = s3.listObjectsV2(
                    ListObjectsV2Request.builder()
                            .bucket(bucket)
                            .prefix(prefix)
                            .continuationToken(token)
                            .build()
            );

            if (list.contents() != null && !list.contents().isEmpty()) {
                var ids = list.contents().stream()
                        .map(o -> ObjectIdentifier.builder().key(o.key()).build())
                        .toList();

                s3.deleteObjects(DeleteObjectsRequest.builder()
                        .bucket(bucket)
                        .delete(Delete.builder().objects(ids).build())
                        .build());
            }

            if (!list.isTruncated()) break;
            token = list.nextContinuationToken();
        }
    }
}
