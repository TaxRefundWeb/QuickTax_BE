package com.quicktax.demo.api;

import com.quicktax.demo.common.ApiResponse;
import com.quicktax.demo.dto.RefundPageResponse;
import com.quicktax.demo.dto.RefundYearRequest;
import com.quicktax.demo.service.refund.RefundSelectionService; // 💡 import 변경
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/refund")
@RequiredArgsConstructor
public class RefundController {

    private final RefundSelectionService refundSelectionService;

    @PostMapping("/selection")
    public ApiResponse<RefundPageResponse> selectRefundYears(
            @AuthenticationPrincipal Long cpaId,
            @RequestBody RefundYearRequest request) {

        // 💡 메서드 호출 객체도 변경
        return ApiResponse.ok(refundSelectionService.configureRefundPages(cpaId, request));
    }
}