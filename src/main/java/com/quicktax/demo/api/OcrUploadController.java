package com.quicktax.demo.api;

import com.quicktax.demo.common.ApiResponse;
import com.quicktax.demo.config.UserDetailsImpl;
import com.quicktax.demo.dto.OcrConfirmRequest;
// 💡 [Main 반영] DTO 패키지 위치가 dto -> dto.ocr 로 변경된 것을 반영
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
@Tag(name = "3. OCR") // [Main 유지] Swagger 태그 유지
public class OcrUploadController {

    private final OcrService ocrService;             // [Feat-53] 확정 및 계산
    private final OcrUploadService ocrUploadService; // [Shared] 업로드
    private final OcrQueryService ocrQueryService;   // [Main] 조회 기능

    // 1. [1단계] 업로드할 URL(출입증) 발급 요청
    // POST /api/cases/{caseId}/ocr/presign
    @PostMapping("/{caseId}/ocr/presign")
    public ApiResponse<OcrPresignResponse> presignUrl(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long caseId
    ) {
        OcrPresignResponse response = ocrUploadService.presign(userDetails.getCpaId(), caseId);
        return ApiResponse.ok(response);
    }

    // 2. [2단계] (프론트가 S3에 직접 올린 뒤) 업로드 완료 알림
    // POST /api/cases/{caseId}/ocr/complete
    @PostMapping("/{caseId}/ocr/complete")
    public ApiResponse<OcrUploadCompleteResponse> completeUpload(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long caseId
    ) {
        OcrUploadCompleteResponse response = ocrUploadService.complete(userDetails.getCpaId(), caseId);
        return ApiResponse.ok(response);
    }

    // 3. [3단계] OCR 결과 확정 및 계산 실행
    // POST /api/cases/{caseId}/ocr
    @PostMapping("/{caseId}/ocr")
    public ApiResponse<String> confirmAndCalculate(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long caseId,
            @RequestBody OcrConfirmRequest request
    ) {
        ocrService.confirmOcrDataAndCalculate(userDetails.getCpaId(), caseId, request);
        return ApiResponse.ok("OCR 데이터가 확정되고 계산이 완료되었습니다.");
    }

    // 4. [조회] OCR 데이터 조회 (Main 브랜치 기능 병합)
    // GET /api/cases/{caseId}/ocr
    @GetMapping("/{caseId}/ocr")
    public ApiResponse<OcrDataResponse> getOcrData(
            @AuthenticationPrincipal UserDetailsImpl userDetails, // UserDetailsImpl로 통일
            @PathVariable Long caseId
    ) {
        // userDetails.getCpaId()를 사용하여 기존 서비스 호출
        return ApiResponse.ok(ocrQueryService.getOcr(userDetails.getCpaId(), caseId));
    }
}