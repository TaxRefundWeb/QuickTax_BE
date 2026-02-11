package com.quicktax.demo.service.ocr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quicktax.demo.common.ApiException;
import com.quicktax.demo.common.ErrorCode;
import com.quicktax.demo.domain.cases.TaxCase;
import com.quicktax.demo.domain.ocr.OcrJob;
import com.quicktax.demo.domain.ocr.OcrJobErrorCode;
import com.quicktax.demo.domain.ocr.OcrJobStatus;
import com.quicktax.demo.ocr.OcrStartMessage;
import com.quicktax.demo.dto.ocr.OcrPresignResponse;
import com.quicktax.demo.dto.ocr.OcrUploadCompleteResponse;
import com.quicktax.demo.repo.TaxCaseRepository;
import com.quicktax.demo.repo.ocr.OcrJobRepository;
import com.quicktax.demo.service.s3.OcrS3KeyService;
import com.quicktax.demo.service.s3.S3PresignService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.time.Instant;

@Service
public class OcrUploadService {

    private final TaxCaseRepository taxCaseRepository;
    private final OcrJobRepository ocrJobRepository;
    private final OcrS3KeyService keyService;
    private final S3PresignService presignService;
    private final S3Client s3Client;

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;

    private final int expiresIn;

    @Value("${app.sqs.queue-url}")
    private String queueUrl;

    public OcrUploadService(
            TaxCaseRepository taxCaseRepository,
            OcrJobRepository ocrJobRepository,
            OcrS3KeyService keyService,
            S3PresignService presignService,
            S3Client s3Client,
            SqsClient sqsClient,
            ObjectMapper objectMapper,
            @Value("${quicktax.s3.presign.expire-seconds:900}") int expiresIn
    ) {
        this.taxCaseRepository = taxCaseRepository;
        this.ocrJobRepository = ocrJobRepository;
        this.keyService = keyService;
        this.presignService = presignService;
        this.s3Client = s3Client;
        this.sqsClient = sqsClient;
        this.objectMapper = objectMapper;
        this.expiresIn = expiresIn;
    }

    /**
     *  S3 HEAD 재시도 3회
     * - 대기: 200ms -> 400ms -> 800ms (총 1.4s)
     */
    private HeadObjectResponse headWithRetry(String bucket, String key) {
        int[] backoffMs = {200, 400, 800};
        RuntimeException last = null;

        for (int attempt = 0; attempt < 4; attempt++) {
            try {
                return s3Client.headObject(HeadObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build());
            } catch (RuntimeException e) {
                last = e;
            }

            if (attempt < 3) {
                try {
                    Thread.sleep(backoffMs[attempt]);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("HEAD retry interrupted", ie);
                }
            }
        }
        throw last;
    }

    private void sendOcrStart(Long caseId, String originalS3Key, Instant requestedAt) throws Exception {
        OcrStartMessage msg = new OcrStartMessage();
        msg.setType("OCR_START");
        msg.setCaseId(caseId);
        msg.setOriginalS3Key(originalS3Key);
        msg.setRequestedAt(requestedAt.toString());

        String body = objectMapper.writeValueAsString(msg);

        sqsClient.sendMessage(SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(body)
                .build());
    }

    private void requireLogin(Long cpaId) {
        if (cpaId == null) throw new ApiException(ErrorCode.AUTH401, "로그인이 필요합니다.");
    }

    private TaxCase requireOwnedCase(Long cpaId, Long caseId) {
        TaxCase taxCase = taxCaseRepository.findById(caseId)
                .orElseThrow(() -> new ApiException(ErrorCode.COMMON404, "존재하지 않는 caseId 입니다."));

        Long ownerCpaId = taxCase.getCustomer().getTaxCompany().getCpaId();
        if (!cpaId.equals(ownerCpaId)) {
            throw new ApiException(ErrorCode.AUTH403, "권한이 없습니다.");
        }
        return taxCase;
    }

    @Transactional
    public OcrPresignResponse presign(Long cpaId, Long caseId) {
        requireLogin(cpaId);
        TaxCase taxCase = requireOwnedCase(cpaId, caseId);

        OcrJob job = ocrJobRepository.findById(caseId)
                .orElseGet(() -> new OcrJob(taxCase));

        String key = (job.getOriginalS3Key() != null && !job.getOriginalS3Key().isBlank())
                ? job.getOriginalS3Key()
                : keyService.rawPdfKey(caseId);

        final String url;
        try {
            url = presignService.presignPutPdf(key);
        } catch (Exception e) {
            throw new ApiException(
                    ErrorCode.COMMON500,
                    "S3 presign 실패: " + e.getClass().getSimpleName() + " - " + (e.getMessage() == null ? "" : e.getMessage())
            );
        }

        job.resetWaitingUpload(key);
        ocrJobRepository.save(job);

        return new OcrPresignResponse(url, key, expiresIn);
    }

    @Transactional
    public OcrUploadCompleteResponse complete(Long cpaId, Long caseId) {
        requireLogin(cpaId);
        requireOwnedCase(cpaId, caseId);

        OcrJob job = ocrJobRepository.findById(caseId)
                .orElseThrow(() -> new ApiException(ErrorCode.COMMON404, "ocr_job 없음: presign 먼저"));

        String key = job.getOriginalS3Key();
        if (key == null || key.isBlank()) {
            throw new ApiException(ErrorCode.COMMON500, "original_s3_key 없음: presign 로직 확인 필요");
        }

        // 멱등: WAITING_UPLOAD 상태에서만 “처리 시작 확정”을 만든다
        if (job.getStatus() != OcrJobStatus.WAITING_UPLOAD) {
            return new OcrUploadCompleteResponse(
                    key, null, null, null,
                    job.getStatus(), job.getErrorCode(), job.getErrorMessage()
            );
        }

        final String bucket = presignService.bucket();
        final HeadObjectResponse head;

        try {
            head = headWithRetry(bucket, key);

        } catch (S3Exception e) {
            // (b) HEAD 최종 404 -> WAITING_UPLOAD 유지
            if (e.statusCode() == 404) {
                job.markUploadNotFound("UPLOAD_NOT_FOUND: key=" + key);
                return new OcrUploadCompleteResponse(
                        key, null, null, null,
                        job.getStatus(), job.getErrorCode(), job.getErrorMessage()
                );
            }

            // (c) 403/5xx 등 -> FAILED + S3_HEAD_ERROR
            job.markFailed(
                    OcrJobErrorCode.S3_HEAD_ERROR.name(),
                    "S3_HEAD_ERROR: status=" + e.statusCode() + ", msg=" +
                            (e.awsErrorDetails() == null ? "" : e.awsErrorDetails().errorMessage())
            );
            return new OcrUploadCompleteResponse(
                    key, null, null, null,
                    job.getStatus(), job.getErrorCode(), job.getErrorMessage()
            );

        } catch (RuntimeException e) {
            job.markFailed(
                    OcrJobErrorCode.S3_HEAD_ERROR.name(),
                    "S3_HEAD_ERROR: " + e.getClass().getSimpleName() + " - " + (e.getMessage() == null ? "" : e.getMessage())
            );
            return new OcrUploadCompleteResponse(
                    key, null, null, null,
                    job.getStatus(), job.getErrorCode(), job.getErrorMessage()
            );
        }

        // (a) HEAD 성공 -> PROCESSING + timestamps
        Instant now = Instant.now();
        job.markProcessing();

        // SQS OCR_START 메시지 전송
        try {
            sendOcrStart(caseId, key, now);
        } catch (Exception e) {
            // 큐잉 실패했는데 PROCESSING 유지하면 운영에서 지옥 열린다 → FAILED로 확정
            job.markFailed(
                    OcrJobErrorCode.QUEUE_SEND_ERROR.name(),
                    "QUEUE_SEND_ERROR: " + e.getClass().getSimpleName() + " - " + (e.getMessage() == null ? "" : e.getMessage())
            );
            return new OcrUploadCompleteResponse(
                    key,
                    head.contentLength(),
                    head.eTag(),
                    head.serverSideEncryptionAsString(),
                    job.getStatus(),
                    job.getErrorCode(),
                    job.getErrorMessage()
            );
        }

        return new OcrUploadCompleteResponse(
                key,
                head.contentLength(),
                head.eTag(),
                head.serverSideEncryptionAsString(),
                job.getStatus(),
                job.getErrorCode(),
                job.getErrorMessage()
        );
    }
}
