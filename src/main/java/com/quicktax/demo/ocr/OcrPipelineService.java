package com.quicktax.demo.ocr;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.core.sync.ResponseTransformer;

import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Service
@RequiredArgsConstructor
public class OcrPipelineService {

    private final S3Client s3;
    // TODO: 여기에 너희 DB repository + OCR 호출 서비스 붙이면 됨

    public void handle(OcrStartMessage msg) throws Exception {
        Long caseId = msg.getCaseId();

        // 1) 임시 파일로 다운로드
        Path temp = Files.createTempFile("quicktax_case_" + caseId + "_", ".pdf");
        try {
            s3.getObject(
                    GetObjectRequest.builder()
                            .bucket(msg.getBucket())
                            .key(msg.getRawS3Key())
                            .build(),
                    ResponseTransformer.toFile(temp)
            );

            log.info("Downloaded: caseId={}, path={}", caseId, temp);

            // 2) 여기서 너희 OCR/파싱 실행
            // - ocr_job.status PROCESSING으로 바꾸고
            // - OCR 돌리고
            // - ocr_result / ocr_result_per_company / calc_result 업데이트
            // - 끝나면 READY, 실패면 FAILED

        } finally {
            try { Files.deleteIfExists(temp); } catch (Exception ignore) {}
        }
    }
}
