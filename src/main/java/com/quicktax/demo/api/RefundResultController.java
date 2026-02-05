package com.quicktax.demo.api;

import com.quicktax.demo.common.ApiResponse;
import com.quicktax.demo.dto.refundResult.RefundResultResponse;
import com.quicktax.demo.service.result.RefundResultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "4. 계산 결과(Result)", description = "경정청구 계산 결과 조회 API")
public class RefundResultController {

    private final RefundResultService refundResultService;

    @GetMapping("/result/{caseId}")
    @Operation(summary = "년도별 계산 결과 조회", description = "특정 Case의 년도별 계산 시나리오(청년, 자녀 등)와 예상 환급액을 조회합니다.")
    public ApiResponse<RefundResultResponse> getCalculationResult(
            @AuthenticationPrincipal Long cpaId,
            @Parameter(description = "조회할 Case ID", required = true) @PathVariable Long caseId) {

        return ApiResponse.ok(refundResultService.getCalculationResult(cpaId, caseId));
    }
}