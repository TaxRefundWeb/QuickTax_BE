package com.quicktax.demo.api;

import com.quicktax.demo.common.ApiResponse;
import com.quicktax.demo.dto.ocr.OcrDataResponse;
import com.quicktax.demo.dto.ocr.OcrPresignResponse;
import com.quicktax.demo.dto.ocr.OcrUploadCompleteResponse;
import com.quicktax.demo.service.ocr.OcrQueryService;
import com.quicktax.demo.service.ocr.OcrUploadService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@Tag(name = "3. OCR")
public class OcrUploadController {

    private final OcrUploadService ocrUploadService;
    private final OcrQueryService ocrQueryService;

    @PostMapping("/api/cases/{caseId}/ocr/presign")
    public ApiResponse<OcrPresignResponse> presign(
            @AuthenticationPrincipal Long cpaId,
            @PathVariable Long caseId
    ) {
        return ApiResponse.ok(ocrUploadService.presign(cpaId, caseId));
    }

    @PostMapping("/api/cases/{caseId}/ocr/complete")
    public ApiResponse<OcrUploadCompleteResponse> complete(
            @AuthenticationPrincipal Long cpaId,
            @PathVariable Long caseId
    ) {
        return ApiResponse.ok(ocrUploadService.complete(cpaId, caseId));
    }

    @GetMapping("/api/cases/{caseId}/ocr")
    public ApiResponse<OcrDataResponse> ocr(
            @AuthenticationPrincipal Long cpaId,
            @PathVariable Long caseId
    ) {
        return ApiResponse.ok(ocrQueryService.getOcr(cpaId, caseId));
    }
}
