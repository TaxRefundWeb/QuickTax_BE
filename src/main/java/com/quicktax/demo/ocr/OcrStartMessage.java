package com.quicktax.demo.ocr;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class OcrStartMessage {
    private String type; // "OCR_START"
    private Long caseId;
}
