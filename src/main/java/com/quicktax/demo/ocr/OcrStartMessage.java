package com.quicktax.demo.ocr;

import lombok.Getter;
import lombok.Setter;

/**
 * Step2(/complete)에서 SQS로 보내는 최소 메시지.
 *
 * 요구사항: caseId, originalS3Key, requestedAt (+ type)
 */
@Getter
@Setter
public class OcrStartMessage {
    private String type;          // "OCR_START"
    private Long caseId;          // caseId = jobId
    private String originalS3Key; // stg/cases/{caseId}/inbox/raw/{jobId}.pdf
    private String requestedAt;   // Instant.toString()
}
