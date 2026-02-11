package com.quicktax.demo.api;

import com.quicktax.demo.common.ApiResponse;
import com.quicktax.demo.dto.refund.RefundResultsResponse;
import com.quicktax.demo.service.result.RefundResultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/result")
@Tag(name = "5. Result (계산 결과 조회)", description = "시나리오별 환급액 조회")
public class RefundResultController {

    private final RefundResultService refundResultService;

    @GetMapping("/{caseId}")
    @Operation(summary = "계산 결과 조회", description = "해당 Case의 모든 연도/시나리오별 계산 결과를 조회합니다.")
    public ApiResponse<RefundResultsResponse> getRefundResults(
            @AuthenticationPrincipal Long cpaId,
            @PathVariable Long caseId
    ) {
        return ApiResponse.ok(refundResultService.getRefundResults(cpaId, caseId));
    }
}
