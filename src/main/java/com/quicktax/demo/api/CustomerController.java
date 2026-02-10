package com.quicktax.demo.api;

import com.quicktax.demo.common.ApiResponse;
import com.quicktax.demo.dto.customer.CustomerCreateRequest;
import com.quicktax.demo.dto.customer.CustomerDetailResponse;
import com.quicktax.demo.dto.customer.CustomerUpdateRequest;
import com.quicktax.demo.dto.customer.CustomersResponse;
import com.quicktax.demo.dto.past.PastDataResponse;
import com.quicktax.demo.service.customer.CustomerService;
import com.quicktax.demo.service.past.RefundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "2. 고객(Customer)", description = "고객 관리(등록, 조회, 수정, 과거기록) API")
public class CustomerController {

    private final CustomerService customerService;
    private final RefundService refundService;

    /**
     * 1. 고객 목록 조회
     * GET /api/customers
     */
    @GetMapping("/customers")
    @Operation(summary = "고객 목록 조회", description = "로그인한 CPA가 담당하는 고객 목록을 조회합니다.")
    public ApiResponse<CustomersResponse> getMyCustomers(@AuthenticationPrincipal Long cpaId) {
        return ApiResponse.ok(customerService.getCustomerList(cpaId));
    }

    /**
     * 2. 신규 고객 등록
     * POST /api/customers/new
     */
    @PostMapping("/customers/new")
    @Operation(summary = "신규 고객 등록", description = "새로운 고객 정보를 등록합니다.")
    public ApiResponse<Long> createCustomer(
            @AuthenticationPrincipal Long cpaId,
            @RequestBody CustomerCreateRequest request) {

        Long customerId = customerService.createCustomer(cpaId, request);
        return ApiResponse.ok(customerId);
    }

    /**
     * 3. 고객 이전 기록 열람
     * GET /api/customers/{customerId}/past
     */
    @GetMapping("/customers/{customerId}/past")
    @Operation(summary = "고객의 과거기록 조회", description = "해당 URL에 담긴 고객의 과거기록을 조회합니다.")
    public ApiResponse<PastDataResponse> getPastRecords(
            @AuthenticationPrincipal Long cpaId,
            @PathVariable Long customerId) {
        return ApiResponse.ok(refundService.getCustomerPastData(cpaId, customerId));
    }

    /**
     * 4. 고객 기본 정보 조회 (상세)
     * GET /api/customers/{customerId}
     */
    @GetMapping("/customers/{customerId}")
    @Operation(summary = "고객 기본 정보 조회", description = "특정 고객의 상세 정보(이름, 주민번호, 은행 정보 등)를 조회합니다.")
    public ApiResponse<CustomerDetailResponse> getCustomerDetail(
            @AuthenticationPrincipal Long cpaId,
            @PathVariable(name = "customerId") Long customerId) {

        return ApiResponse.ok(customerService.getCustomerDetail(cpaId, customerId));
    }

    /**
     * 5. 고객 기본 정보 수정
     * PATCH /api/customers/{customerId}
     */
    @PatchMapping("/customers/{customerId}")
    @Operation(summary = "고객 기본 정보 수정", description = "특정 고객의 정보를 수정합니다. (변경할 필드만 요청 본문에 포함)")
    public ApiResponse<CustomerDetailResponse> updateCustomer(
            @AuthenticationPrincipal Long cpaId,
            @PathVariable(name = "customerId") Long customerId,
            @RequestBody CustomerUpdateRequest request) {

        CustomerDetailResponse updatedDetail = customerService.updateCustomerInfo(cpaId, customerId, request);
        return ApiResponse.ok(updatedDetail);
    }
}