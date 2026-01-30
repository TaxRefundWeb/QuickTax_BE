package com.quicktax.demo.api;

import com.quicktax.demo.common.ApiResponse;
import com.quicktax.demo.dto.CustomerCreateRequest;
import com.quicktax.demo.dto.CustomersResponse;
import com.quicktax.demo.dto.PastDataResponse;
import com.quicktax.demo.service.MainService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/main")
@RequiredArgsConstructor
public class MainController {

    private final MainService mainService;

    /**
     * 1. 고객 목록 조회
     */
    @GetMapping("/customers")
    public ApiResponse<CustomersResponse> getMyCustomers(@AuthenticationPrincipal Long cpaId) {
        return ApiResponse.ok(mainService.getCustomerList(cpaId));
    }

    /**
     * 2. 신규 고객 등록
     */
    @PostMapping("/customers")
    public ApiResponse<Long> createCustomer(
            @AuthenticationPrincipal Long cpaId,
            @RequestBody CustomerCreateRequest request) {

        Long customerId = mainService.createCustomer(cpaId, request);
        return ApiResponse.ok(customerId);
    }

    /**
     * 3. 고객 이전 기록 열람 (보안 검증 포함)
     * @AuthenticationPrincipal을 통해 로그인한 cpaId를 받아 서비스에 전달합니다.
     */
    @GetMapping("/customers/{customerId}/past")
    public ApiResponse<PastDataResponse> getPastRecords(
            @AuthenticationPrincipal Long cpaId,
            @PathVariable(name = "customerId") Long customerId) {

        return ApiResponse.ok(mainService.getCustomerPastData(cpaId, customerId));
    }
}