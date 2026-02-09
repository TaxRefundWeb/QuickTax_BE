package com.quicktax.demo.api;

import com.quicktax.demo.common.ApiResponse;
import com.quicktax.demo.dto.RefundResultsResponse;

import com.quicktax.demo.service.result.RefundResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class RefundResultController {

    private final RefundResultService refundResultService;

    @GetMapping("/result/{caseId}")
    public ApiResponse<RefundResultsResponse> getRefundResults(
            @AuthenticationPrincipal Long cpaId,
            @PathVariable Long caseId
    ) {
        return ApiResponse.ok(refundResultService.getRefundResults(cpaId, caseId));
    }
}
