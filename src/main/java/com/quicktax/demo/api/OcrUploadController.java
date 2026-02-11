package com.quicktax.demo.api;

import com.quicktax.demo.common.ApiResponse;
import com.quicktax.demo.dto.OcrConfirmRequest;
import com.quicktax.demo.dto.ocr.OcrDataResponse;
import com.quicktax.demo.dto.ocr.OcrPresignResponse;
import com.quicktax.demo.dto.ocr.OcrUploadCompleteResponse;
import com.quicktax.demo.service.ocr.OcrQueryService;
import com.quicktax.demo.service.ocr.OcrService;
import com.quicktax.demo.service.ocr.OcrUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cases")
@RequiredArgsConstructor
@Tag(name = "3. OCR", description = "주소받기, 상태확인, 결과확인, 결과확정")
public class OcrUploadController {

    private final OcrService ocrService;
    private final OcrUploadService ocrUploadService;
    private final OcrQueryService ocrQueryService;

    @Operation(summary = "주소 받기", description = "FE가 파일을 올릴 S3주소를 받아온다.")
    @PostMapping("/{caseId}/ocr/presign")
    public ApiResponse<OcrPresignResponse> presignUrl(
            @AuthenticationPrincipal Long cpaId,
            @PathVariable Long caseId
    ) {
        OcrPresignResponse response = ocrUploadService.presign(cpaId, caseId);
        return ApiResponse.ok(response);
    }

    @PostMapping("/{caseId}/ocr/complete")
    @Operation(summary = "상태 확인", description = "S3전송을 완료하고, OCR진행사항 등을 알기위한 API")
    public ApiResponse<OcrUploadCompleteResponse> completeUpload(
            @AuthenticationPrincipal Long cpaId,
            @PathVariable Long caseId
    ) {
        OcrUploadCompleteResponse response = ocrUploadService.complete(cpaId, caseId);
        return ApiResponse.ok(response);
    }

    @GetMapping("/{caseId}/ocr")
    @Operation(summary = "OCR 결과 확인창", description = "각 년도별 저장된 파일의 URL과 OCR결과를 받아온다.")
    public ApiResponse<OcrDataResponse> getOcrData(
            @AuthenticationPrincipal Long cpaId,
            @PathVariable Long caseId
    ) {
        return ApiResponse.ok(ocrQueryService.getOcr(cpaId, caseId));
    }

    @Operation(summary = "OCR 결과 확정", description = "OCR결과 검토를 마치고 확정한다.")
    @PostMapping("/{caseId}/ocr")
    public ApiResponse<String> confirmAndCalculate(
            @AuthenticationPrincipal Long cpaId,
            @PathVariable Long caseId,
            @RequestBody OcrConfirmRequest request
    ) {
        ocrService.confirmOcrDataAndCalculate(cpaId, caseId, request);
        return ApiResponse.ok("OCR 데이터가 확정되고 계산이 완료되었습니다.");
    }


}
