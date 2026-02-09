package com.quicktax.demo.service.ocr;

import com.quicktax.demo.domain.cases.TaxCase;
import com.quicktax.demo.domain.ocr.OcrJob;
import com.quicktax.demo.domain.ocr.OcrJobStatus;
import com.quicktax.demo.dto.OcrPresignResponse;
import com.quicktax.demo.dto.OcrUploadCompleteResponse;
import com.quicktax.demo.repo.OcrJobRepository;
import com.quicktax.demo.repo.TaxCaseRepository;
import com.quicktax.demo.service.s3.OcrS3KeyService;
import com.quicktax.demo.service.s3.S3PresignService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;

@Service
public class OcrUploadService {

    private final TaxCaseRepository taxCaseRepository;
    private final OcrJobRepository ocrJobRepository;
    private final OcrS3KeyService keyService;
    private final S3PresignService presignService;
    private final S3Client s3Client;
    private final int expiresIn;

    public OcrUploadService(
            TaxCaseRepository taxCaseRepository,
            OcrJobRepository ocrJobRepository,
            OcrS3KeyService keyService,
            S3PresignService presignService,
            S3Client s3Client,
            @Value("${quicktax.s3.presign.expire-seconds:900}") int expiresIn
    ) {
        this.taxCaseRepository = taxCaseRepository;
        this.ocrJobRepository = ocrJobRepository;
        this.keyService = keyService;
        this.presignService = presignService;
        this.s3Client = s3Client;
        this.expiresIn = expiresIn;
    }

    @Transactional
    public OcrPresignResponse presign(Long caseId) {
        TaxCase taxCase = taxCaseRepository.findById(caseId)
                .orElseThrow(() -> new IllegalArgumentException("cases 없음: " + caseId));

        String key = keyService.rawPdfKey(caseId);
        String url = presignService.presignPutPdf(key);

        OcrJob job = ocrJobRepository.findById(caseId).orElse(new OcrJob(taxCase));
        job.resetWaitingUpload(key); // WAITING_UPLOAD + key 세팅
        ocrJobRepository.save(job);

        return new OcrPresignResponse(url, key, expiresIn);
    }

    @Transactional
    public OcrUploadCompleteResponse complete(Long caseId) {
        OcrJob job = ocrJobRepository.findById(caseId)
                .orElseThrow(() -> new IllegalArgumentException("ocr_job 없음: presign 먼저"));

        String key = job.getOriginalS3Key();
        HeadObjectResponse head = s3Client.headObject(HeadObjectRequest.builder()
                .bucket(presignService.bucket())
                .key(key)
                .build());

        job.markProcessing(); // PROCESSING
        return new OcrUploadCompleteResponse(
                key,
                head.contentLength(),
                head.eTag(),
                head.serverSideEncryptionAsString(),
                job.getStatus()
        );
    }
}
