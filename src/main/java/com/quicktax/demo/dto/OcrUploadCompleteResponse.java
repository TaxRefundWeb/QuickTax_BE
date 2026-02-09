package com.quicktax.demo.dto;

import com.quicktax.demo.domain.ocr.OcrJobStatus;

public record OcrUploadCompleteResponse(
        String s3Key,
        long contentLength,
        String eTag,
        String serverSideEncryption,
        OcrJobStatus status
) {}
