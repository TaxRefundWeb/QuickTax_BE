package com.quicktax.demo.domain.ocr;

import io.swagger.v3.oas.models.info.Contact;

public enum OcrJobErrorCode {
    UPLOAD_NOT_FOUND,
    S3_HEAD_ERROR,
    QUEUE_SEND_ERROR,
    WORKER_EXCEPTION,

    PAGE_RULE_MISMATCH,
    OCR_CALL_FAILED,
    OCR_PARSE_ERROR,

    WORKER_TIMEOUT;
}
