package com.quicktax.demo.api;

import com.quicktax.demo.common.ApiResponse;
import com.quicktax.demo.dto.CustomerCreateRequest;
import com.quicktax.demo.dto.CustomersResponse;
import com.quicktax.demo.service.MainService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/main")
@RequiredArgsConstructor
public class MainController {

    private final MainService mainService;

    /**
     * 고객 목록 조회 (GET)
     */
    @GetMapping("/customers")
    public ApiResponse<CustomersResponse> getMyCustomers(@AuthenticationPrincipal Long cpaId) {
        return ApiResponse.ok(mainService.getCustomerList(cpaId));
    }

    /**
     * 신규 고객 등록 (POST)
     */
    @PostMapping("/customers")
    public ApiResponse<Long> createCustomer(
            @AuthenticationPrincipal Long cpaId,
            @RequestBody CustomerCreateRequest request) { // 💡 JSON 데이터를 객체로 변환

        Long customerId = mainService.createCustomer(cpaId, request);
        return ApiResponse.ok(customerId);
    }
}