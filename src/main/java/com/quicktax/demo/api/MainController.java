package com.quicktax.demo.api;

import com.quicktax.demo.common.ApiResponse;
import com.quicktax.demo.dto.*;
import com.quicktax.demo.service.MainService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/main")
@RequiredArgsConstructor
public class MainController {

    // 💡 에러 수정: 타입명을 MainService(대문자)로 변경했습니다.
    private final MainService mainService;

    /**
     * 1. 고객 목록 조회
     * 로그인한 세무사가 관리하는 전체 고객 리스트를 조회합니다.
     */
    @GetMapping("/customers")
    public ApiResponse<CustomersResponse> getMyCustomers(@AuthenticationPrincipal Long cpaId) {
        return ApiResponse.ok(mainService.getCustomerList(cpaId));
    }

    /**
     * 2. 신규 고객 등록
     * 새로운 고객의 인적 사항 및 수수료 정보를 등록합니다.
     */
    @PostMapping("/customers")
    public ApiResponse<Long> createCustomer(
            @AuthenticationPrincipal Long cpaId,
            @RequestBody CustomerCreateRequest request) {

        Long customerId = mainService.createCustomer(cpaId, request);
        return ApiResponse.ok(customerId);
    }

    /**
     * 3. 고객 이전 기록 열람
     * 특정 고객의 과거 환급 사례(PastData) 목록을 조회합니다.
     */
    @GetMapping("/customers/{customerId}/past")
    public ApiResponse<PastDataResponse> getPastRecords(
            @AuthenticationPrincipal Long cpaId,
            @PathVariable(name = "customerId") Long customerId) {

        return ApiResponse.ok(mainService.getCustomerPastData(cpaId, customerId));
    }

    /**
     * 4. 고객 기본 정보 조회
     * 수정 화면 진입 시 이전에 입력했던 고객의 기본 데이터를 불러옵니다.
     * Response Body는 모든 필드가 String인 스네이크 케이스 규격을 따릅니다.
     */
    @GetMapping("/customers/{customerId}")
    public ApiResponse<CustomerDetailResponse> getCustomerDetail(
            @AuthenticationPrincipal Long cpaId,
            @PathVariable(name = "customerId") Long customerId) {

        return ApiResponse.ok(mainService.getCustomerDetail(cpaId, customerId));
    }

    /**
     * 5. 고객 기본 정보 수정
     * 사용자가 수정한 정보를 반영하고, 수정된 전체 고객 데이터를 반환합니다.
     * PATCH /api/main/customers/{customerId}
     * Request/Response Body 규격: { "name": "...", "bank_number": "...", "final_fee_percent": "..." }
     */
    @PatchMapping("/customers/{customerId}")
    public ApiResponse<CustomerDetailResponse> updateCustomer(
            @AuthenticationPrincipal Long cpaId,
            @PathVariable(name = "customerId") Long customerId,
            @RequestBody CustomerUpdateRequest request) {

        // 서비스에서 수정 처리 후, 최신화된 CustomerDetailResponse(전 필드 String)를 반환받습니다.
        CustomerDetailResponse updatedDetail = mainService.updateCustomerInfo(cpaId, customerId, request);

        return ApiResponse.ok(updatedDetail);
    }
}