package com.quicktax.demo.dto.ocr;

import com.quicktax.demo.domain.ocr.OcrJobStatus;

public record OcrUploadCompleteResponse(
        String s3Key,
        Long contentLength,
        String eTag,
        String serverSideEncryption,
        OcrJobStatus status,
        String errorCode,
        String errorMessage
) {}
