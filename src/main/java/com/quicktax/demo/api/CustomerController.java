package com.quicktax.demo.api;

import com.quicktax.demo.common.ApiResponse;
import com.quicktax.demo.domain.customer.Customer;
import com.quicktax.demo.repo.CustomerRepository;
import io.swagger.v3.oas.annotations.Operation; // 💡 import 추가
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
// 💡 Tag 설명 보완
@Tag(name = "2. 고객(Customer)", description = "고객 목록 조회 및 관리 API")
public class CustomerController {

    private final CustomerRepository customerRepository;

    public CustomerController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @GetMapping("/customers")
    // 💡 Operation 추가: 파라미터 유무에 따른 동작 설명 포함
    @Operation(summary = "고객 목록 조회", description = "전체 고객 목록을 조회하거나, 특정 CPA ID(cpaId)로 필터링하여 조회합니다.")
    public ApiResponse<CustomersResponse> customers(@RequestParam(required = false) Long cpaId) {
        List<Customer> list = (cpaId == null)
                ? customerRepository.findAll()
                : customerRepository.findByTaxCompany_CpaId(cpaId);
        List<CustomerDto> customers = list.stream().map(CustomerDto::new).toList();
        return ApiResponse.ok(new CustomersResponse(customers));
    }

    public record CustomersResponse(List<CustomerDto> customers) {}


    @Getter
    public static class CustomerDto {
        private final Long customerId;
        private final Long cpaId;
        private final String name;
        private final String rrn;
        private final String bank;
        private final String bankNumber;

        public CustomerDto(Customer c) {
            this.customerId = c.getCustomerId();
            this.cpaId = c.getTaxCompany().getCpaId();
            this.name = c.getName();
            this.rrn = c.getRrn();
            this.bank = c.getBank();
            this.bankNumber = c.getBankNumber();
        }
    }
}