package com.quicktax.demo.api;

import com.quicktax.demo.common.ApiResponse;
import com.quicktax.demo.dto.*;
import com.quicktax.demo.service.customer.CustomerService;
import com.quicktax.demo.service.past.RefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/main")
@RequiredArgsConstructor
public class MainController {

    // 💡 분리된 두 서비스를 각각 주입받습니다.
    private final CustomerService customerService;
    private final RefundService refundService;

    /**
     * 1. 고객 목록 조회
     */
    @GetMapping("/customers")
    public ApiResponse<CustomersResponse> getMyCustomers(@AuthenticationPrincipal Long cpaId) {
        return ApiResponse.ok(customerService.getCustomerList(cpaId));
    }

    /**
     * 2. 신규 고객 등록
     * 경로: /api/main/customers/new
     */
    @PostMapping("/customers/new")
    public ApiResponse<Long> createCustomer(
            @AuthenticationPrincipal Long cpaId,
            @RequestBody CustomerCreateRequest request) {

        Long customerId = customerService.createCustomer(cpaId, request);
        return ApiResponse.ok(customerId);
    }

    /**
     * 3. 고객 이전 기록 열람 (RefundService 호출)
     */
    @GetMapping("/customers/{customerId}/past")
    public ApiResponse<PastDataResponse> getPastRecords(
            @AuthenticationPrincipal Long cpaId,
            @PathVariable(name = "customerId") Long customerId) {

        return ApiResponse.ok(refundService.getCustomerPastData(cpaId, customerId));
    }

    /**
     * 4. 고객 기본 정보 조회 (CustomerService 호출)
     */
    @GetMapping("/customers/{customerId}")
    public ApiResponse<CustomerDetailResponse> getCustomerDetail(
            @AuthenticationPrincipal Long cpaId,
            @PathVariable(name = "customerId") Long customerId) {

        return ApiResponse.ok(customerService.getCustomerDetail(cpaId, customerId));
    }

    /**
     * 5. 고객 기본 정보 수정 (CustomerService 호출)
     */
    @PatchMapping("/customers/{customerId}")
    public ApiResponse<CustomerDetailResponse> updateCustomer(
            @AuthenticationPrincipal Long cpaId,
            @PathVariable(name = "customerId") Long customerId,
            @RequestBody CustomerUpdateRequest request) {

        CustomerDetailResponse updatedDetail = customerService.updateCustomerInfo(cpaId, customerId, request);
        return ApiResponse.ok(updatedDetail);
    }
}