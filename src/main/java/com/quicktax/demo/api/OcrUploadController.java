package com.quicktax.demo.api;

import com.quicktax.demo.common.ApiResponse;
import com.quicktax.demo.dto.OcrPresignResponse;
import com.quicktax.demo.dto.OcrUploadCompleteResponse;
import com.quicktax.demo.service.ocr.OcrUploadService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "3. OCR")
public class OcrUploadController {

    private final OcrUploadService ocrUploadService;

    @PostMapping("/api/cases/{caseId}/ocr/presign")
    public ApiResponse<OcrPresignResponse> presign(@PathVariable Long caseId) {
        return ApiResponse.ok(ocrUploadService.presign(caseId));
    }

    @PostMapping("/api/cases/{caseId}/ocr/complete")
    public ApiResponse<OcrUploadCompleteResponse> complete(@PathVariable Long caseId) {
        return ApiResponse.ok(ocrUploadService.complete(caseId));
    }
}
