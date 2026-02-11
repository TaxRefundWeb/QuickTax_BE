package com.quicktax.demo.domain.ocr;

import com.quicktax.demo.domain.cases.TaxCase;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ocr_job")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OcrJob {

    @Id
    @Column(name = "case_id")
    private Long caseId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "case_id", nullable = false)
    private TaxCase taxCase;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private OcrJobStatus status = OcrJobStatus.WAITING_UPLOAD;

    @Column(name = "original_s3_key", columnDefinition = "text")
    private String originalS3Key;

    @Column(name = "error_code", length = 64)
    private String errorCode;

    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;

    public OcrJob(TaxCase taxCase) {
        this.taxCase = taxCase;
        this.status = OcrJobStatus.WAITING_UPLOAD;
    }

    public void resetWaitingUpload(String s3Key) {
        this.status = OcrJobStatus.WAITING_UPLOAD;
        this.originalS3Key = s3Key;
        this.errorCode = null;
        this.errorMessage = null;
    }

    public void markFailed(String errorCode, String errorMessage) {
        this.status = OcrJobStatus.FAILED;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public void markReady() {
        this.status = OcrJobStatus.READY;
    }

    public void markProcessing() {
        this.status = OcrJobStatus.PROCESSING;
        this.errorCode = null;
        this.errorMessage = null;
    }

    public void markUploadNotFound(String message) {
        this.status = OcrJobStatus.WAITING_UPLOAD;
        this.errorCode = OcrJobErrorCode.UPLOAD_NOT_FOUND.name();
        this.errorMessage = message;
    }
}
