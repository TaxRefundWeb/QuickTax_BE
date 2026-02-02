package com.quicktax.demo.api;

import com.quicktax.demo.common.ApiResponse;
import com.quicktax.demo.dto.RefundInputRequest;
import com.quicktax.demo.dto.RefundPageResponse;
import com.quicktax.demo.dto.RefundYearRequest;
import com.quicktax.demo.service.refund.RefundSelectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/refund")
@RequiredArgsConstructor
public class RefundController {

    private final RefundSelectionService refundSelectionService;

    /**
     * 1. 경정청구 기간 선택
     */
    @PostMapping("/selection")
    public ApiResponse<RefundPageResponse> selectRefundYears(
            @AuthenticationPrincipal Long cpaId,
            @RequestBody RefundYearRequest request) {

        return ApiResponse.ok(refundSelectionService.configureRefundPages(cpaId, request));
    }

    /**
     * 2. 경정청구 상세 정보 입력 (플랫 구조 적용)
     */
    @PostMapping("/info")
    public ApiResponse<String> inputRefundInfo(
            @AuthenticationPrincipal Long cpaId,
            @RequestBody RefundInputRequest request) {

        refundSelectionService.saveRefundInfo(cpaId, request);
        return ApiResponse.ok("정보 입력이 완료되었습니다.");
    }
}