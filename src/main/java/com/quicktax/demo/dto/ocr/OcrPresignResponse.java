package com.quicktax.demo.dto.ocr;

public record OcrPresignResponse(
        String uploadUrl,
        String s3Key,
        int expiresIn
) {}
