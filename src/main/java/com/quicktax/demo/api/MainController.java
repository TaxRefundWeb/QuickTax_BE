package com.quicktax.demo.api;

import com.quicktax.demo.common.ApiResponse;
import com.quicktax.demo.dto.*;
import com.quicktax.demo.service.customer.CustomerService;
import com.quicktax.demo.service.past.RefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api") // 💡 "/api/main"에서 "/main"을 제거했습니다.
@RequiredArgsConstructor
public class MainController {

    private final CustomerService customerService;
    private final RefundService refundService;

    /**
     * 1. 고객 목록 조회
     * 변경 전: GET /api/main/customers
     * 변경 후: GET /api/customers
     */
    @GetMapping("/customers")
    public ApiResponse<CustomersResponse> getMyCustomers(@AuthenticationPrincipal Long cpaId) {
        return ApiResponse.ok(customerService.getCustomerList(cpaId));
    }

    /**
     * 2. 신규 고객 등록
     * 변경 전: POST /api/main/customers/new
     * 변경 후: POST /api/customers/new
     */
    @PostMapping("/customers/new")
    public ApiResponse<Long> createCustomer(
            @AuthenticationPrincipal Long cpaId,
            @RequestBody CustomerCreateRequest request) {

        Long customerId = customerService.createCustomer(cpaId, request);
        return ApiResponse.ok(customerId);
    }

    /**
     * 3. 고객 이전 기록 열람
     * 변경 후: GET /api/customers/{customerId}/past
     */
    @GetMapping("/customers/{customerId}/past")
    public ApiResponse<PastDataResponse> getPastRecords(
            @AuthenticationPrincipal Long cpaId,
            @PathVariable(name = "customerId") Long customerId) {

        return ApiResponse.ok(refundService.getCustomerPastData(cpaId, customerId));
    }

    /**
     * 4. 고객 기본 정보 조회
     * 변경 후: GET /api/customers/{customerId}
     */
    @GetMapping("/customers/{customerId}")
    public ApiResponse<CustomerDetailResponse> getCustomerDetail(
            @AuthenticationPrincipal Long cpaId,
            @PathVariable(name = "customerId") Long customerId) {

        return ApiResponse.ok(customerService.getCustomerDetail(cpaId, customerId));
    }

    /**
     * 5. 고객 기본 정보 수정
     * 변경 후: PATCH /api/customers/{customerId}
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