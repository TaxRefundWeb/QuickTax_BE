package com.quicktax.demo.api;

import com.quicktax.demo.common.ApiResponse;
import com.quicktax.demo.config.UserDetailsImpl;
import com.quicktax.demo.dto.calc.CalcConfirmRequest;
import com.quicktax.demo.dto.calc.CalcDocumentResponse;
import com.quicktax.demo.service.calc.ResultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid; // 💡 유효성 검증(@Valid)을 위해 추가
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/result")
@RequiredArgsConstructor
@Tag(name = "4. Result (결과 확정)")
public class ResultController {

    private final ResultService resultService;

    @Operation(summary = "계산식 확정 및 결과 파일 생성 요청")
    @PostMapping("/{caseId}")
    public ApiResponse<String> confirmCalculation(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long caseId,
            @Valid @RequestBody CalcConfirmRequest request // 💡 @Valid 추가 (BEDREQ400 처리용)
    ) {
        // 서비스 호출 (저장 및 파일 생성)
        resultService.confirmAndGenerateFiles(userDetails.getCpaId(), caseId, request);
        return ApiResponse.ok("계산식이 확정되고 결과 파일 생성이 완료되었습니다.");
    }

    @Operation(summary = "최종 완료 결과(문서 및 환급액) 조회")
    @GetMapping("/{caseId}/documents")
    public ApiResponse<CalcDocumentResponse> getResultDocuments(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long caseId
    ) {
        // 서비스 호출 (조회)
        CalcDocumentResponse response = resultService.getResultDocuments(userDetails.getCpaId(), caseId);

        return ApiResponse.ok(response);
    }
}