package com.quicktax.demo.ocr;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class OcrStartMessage {
    private String type;     // "OCR_START"
    private String env;      // "stg"
    private Long caseId;     // 8
    private String bucket;   // quicktax-uploads-...
    private String rawS3Key; // stg/cases/8/inbox/raw/....pdf
}

