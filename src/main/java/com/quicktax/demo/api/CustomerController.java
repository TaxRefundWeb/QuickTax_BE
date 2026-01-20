package com.quicktax.demo.api;

import com.quicktax.demo.common.ApiResponse;
import com.quicktax.demo.domain.Customer;
import com.quicktax.demo.repo.CustomerRepository;
import lombok.Getter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CustomerController {

    private final CustomerRepository customerRepository;

    public CustomerController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @GetMapping("/customers")
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
        private final String rrnEnc;
        private final String bankCode;
        private final String accountNumberEnc;

        public CustomerDto(Customer c) {
            this.customerId = c.getCustomerId();
            this.cpaId = c.getTaxCompany().getCpaId();
            this.name = c.getName();
            this.rrnEnc = c.getRrnEnc();
            this.bankCode = c.getBankCode();
            this.accountNumberEnc = c.getAccountNumberEnc();
        }
    }
}
