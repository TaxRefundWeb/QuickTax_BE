package com.quicktax.demo.service.ocr;

import com.quicktax.demo.common.ApiException;
import com.quicktax.demo.common.ErrorCode;
import com.quicktax.demo.domain.cases.TaxCase;
import com.quicktax.demo.domain.ocr.OcrJob;
import com.quicktax.demo.domain.ocr.OcrJobStatus;
import com.quicktax.demo.dto.ocr.OcrPresignResponse;
import com.quicktax.demo.dto.ocr.OcrUploadCompleteResponse;
import com.quicktax.demo.repo.ocr.OcrJobRepository;
import com.quicktax.demo.repo.TaxCaseRepository;
import com.quicktax.demo.service.s3.OcrS3KeyService;
import com.quicktax.demo.service.s3.S3PresignService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

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

        final HeadObjectResponse head;


        if (job.getStatus() != OcrJobStatus.WAITING_UPLOAD) {
            return new OcrUploadCompleteResponse(
                    job.getOriginalS3Key(), null, null, null,
                    job.getStatus(), job.getErrorCode(), job.getErrorMessage()
            );
        }

        try {
            head = s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(presignService.bucket())
                    .key(key)
                    .build());
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                job.markUploadNotFound("UPLOAD_NOT_FOUND: ... key=" + key);
                return new OcrUploadCompleteResponse(
                        key, null, null, null,
                        job.getStatus(), job.getErrorCode(), job.getErrorMessage()
                );
            }
            throw new ApiException(ErrorCode.COMMON500, "S3 headObject 실패: ...");
        }

        job.markProcessing();
        return new OcrUploadCompleteResponse(
                key, head.contentLength(), head.eTag(), head.serverSideEncryptionAsString(),
                job.getStatus(), job.getErrorCode(), job.getErrorMessage()
        );

    }
}
