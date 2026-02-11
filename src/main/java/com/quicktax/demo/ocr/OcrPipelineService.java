package com.quicktax.demo.ocr;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Service
@RequiredArgsConstructor
public class OcrPipelineService {

    private final S3Client s3;


    @Value("${quicktax.s3.bucket}")
    private String bucket;

    public void handle(OcrStartMessage msg) throws Exception {
        Long caseId = msg.getCaseId();
        String key = msg.getOriginalS3Key();
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("originalS3Key is blank");
        }

        Path temp = Files.createTempFile("quicktax_case_" + caseId + "_", ".pdf");
        try {
            s3.getObject(
                    GetObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .build(),
                    ResponseTransformer.toFile(temp)
            );

            log.info("Downloaded: caseId={}, path={}", caseId, temp);

            // TODO: Step3~ 이후 OCR/파싱/DB업데이트 붙이면 됨
        } finally {
            try { Files.deleteIfExists(temp); } catch (Exception ignore) {}
        }
    }
}
