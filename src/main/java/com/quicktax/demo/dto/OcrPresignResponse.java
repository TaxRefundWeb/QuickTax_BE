package com.quicktax.demo.dto;

public record OcrPresignResponse(
        String uploadUrl,
        String s3Key,
        int expiresIn
) {}
