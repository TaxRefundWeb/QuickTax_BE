package com.quicktax.demo.api;

import com.quicktax.demo.common.ApiResponse;
import com.quicktax.demo.config.UserDetailsImpl;
import com.quicktax.demo.dto.OcrConfirmRequest;
import com.quicktax.demo.dto.OcrPresignResponse;         // 💡 추가
import com.quicktax.demo.dto.OcrUploadCompleteResponse;  // 💡 추가
import com.quicktax.demo.service.ocr.OcrService;
import com.quicktax.demo.service.ocr.OcrUploadService;   // 💡 질문자님 코드 주입
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cases")
@RequiredArgsConstructor
public class OcrUploadController {

    private final OcrService ocrService;             // 확정 및 계산 담당
    private final OcrUploadService ocrUploadService; // 💡 업로드(Presign) 담당

    // 1. [1단계] 업로드할 URL(출입증) 발급 요청
    // POST /api/cases/{caseId}/ocr/presign
    @PostMapping("/{caseId}/ocr/presign")
    public ApiResponse<OcrPresignResponse> presignUrl(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long caseId
    ) {
        // 질문자님 코드의 presign 호출
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
        // 질문자님 코드의 complete 호출 (S3 확인 및 상태 변경)
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
        // 이건 아까 만든 OcrService 그대로 사용
        ocrService.confirmOcrDataAndCalculate(userDetails.getCpaId(), caseId, request);
        return ApiResponse.ok("OCR 데이터가 확정되고 계산이 완료되었습니다.");
    }
}