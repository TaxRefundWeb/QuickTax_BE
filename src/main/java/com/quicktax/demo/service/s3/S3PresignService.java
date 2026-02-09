package com.quicktax.demo.service.s3;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;

@Service
public class S3PresignService {

    private final S3Presigner presigner;
    private final String bucket;
    private final int expireSeconds;

    public S3PresignService(
            S3Presigner presigner,
            @Value("${quicktax.s3.bucket}") String bucket,
            @Value("${quicktax.s3.presign.expire-seconds:900}") int expireSeconds
    ) {
        this.presigner = presigner;
        this.bucket = bucket.trim();
        this.expireSeconds = expireSeconds;
    }

    public String bucket() { return bucket; }

    public String presignPutPdf(String key) {
        PutObjectRequest putReq = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType("application/pdf")
                .build();

        PutObjectPresignRequest presignReq = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(expireSeconds))
                .putObjectRequest(putReq)
                .build();

        PresignedPutObjectRequest presigned = presigner.presignPutObject(presignReq);
        return presigned.url().toString();
    }
}
