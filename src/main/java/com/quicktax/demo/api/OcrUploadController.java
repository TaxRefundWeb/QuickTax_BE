package com.quicktax.demo.api;

import com.quicktax.demo.common.ApiResponse;
import com.quicktax.demo.dto.OcrConfirmRequest;
import com.quicktax.demo.dto.ocr.OcrDataResponse;
import com.quicktax.demo.dto.ocr.OcrPresignResponse;
import com.quicktax.demo.dto.ocr.OcrUploadCompleteResponse;
import com.quicktax.demo.service.ocr.OcrQueryService;
import com.quicktax.demo.service.ocr.OcrService;
import com.quicktax.demo.service.ocr.OcrUploadService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cases")
@RequiredArgsConstructor
@Tag(name = "3. OCR")
public class OcrUploadController {

    private final OcrService ocrService;
    private final OcrUploadService ocrUploadService;
    private final OcrQueryService ocrQueryService;

    @PostMapping("/{caseId}/ocr/presign")
    public ApiResponse<OcrPresignResponse> presignUrl(
            @AuthenticationPrincipal Long cpaId,
            @PathVariable Long caseId
    ) {
        OcrPresignResponse response = ocrUploadService.presign(cpaId, caseId);
        return ApiResponse.ok(response);
    }

    @PostMapping("/{caseId}/ocr/complete")
    public ApiResponse<OcrUploadCompleteResponse> completeUpload(
            @AuthenticationPrincipal Long cpaId,
            @PathVariable Long caseId
    ) {
        OcrUploadCompleteResponse response = ocrUploadService.complete(cpaId, caseId);
        return ApiResponse.ok(response);
    }

    @PostMapping("/{caseId}/ocr")
    public ApiResponse<String> confirmAndCalculate(
            @AuthenticationPrincipal Long cpaId,
            @PathVariable Long caseId,
            @RequestBody OcrConfirmRequest request
    ) {
        ocrService.confirmOcrDataAndCalculate(cpaId, caseId, request);
        return ApiResponse.ok("OCR 데이터가 확정되고 계산이 완료되었습니다.");
    }

    @GetMapping("/{caseId}/ocr")
    public ApiResponse<OcrDataResponse> getOcrData(
            @AuthenticationPrincipal Long cpaId,
            @PathVariable Long caseId
    ) {
        return ApiResponse.ok(ocrQueryService.getOcr(cpaId, caseId));
    }
}
