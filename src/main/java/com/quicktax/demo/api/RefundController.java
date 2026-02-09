package com.quicktax.demo.api;

import com.quicktax.demo.common.ApiResponse;
import com.quicktax.demo.dto.refund.*;
import com.quicktax.demo.service.refund.RefundSelectionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "4. Refund(경정청구되어있는 태그)")
public class RefundController {

    private final RefundSelectionService refundSelectionService;

    @PostMapping("/refund-selection/{customerId}")
    public ApiResponse<RefundSelectionResponse> selectRefundPeriod(
            @AuthenticationPrincipal Long cpaId,
            @PathVariable Long customerId,
            @RequestBody RefundSelectionRequest request
    ) {
        Long caseId = refundSelectionService.createCase(cpaId, customerId, request);
        return ApiResponse.ok(new RefundSelectionResponse(caseId));
    }

    @PostMapping("/refund-claims/{caseId}")
    public ApiResponse<RefundClaimResponse> submitRefundClaims(
            @AuthenticationPrincipal Long cpaId,
            @PathVariable Long caseId,
            @RequestBody RefundClaimRequest request
    ) {
        return ApiResponse.ok(refundSelectionService.saveRefundClaims(cpaId, caseId, request));
    }
}
