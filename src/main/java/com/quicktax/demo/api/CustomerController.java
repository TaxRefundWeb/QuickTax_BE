package com.quicktax.demo.api;

import com.quicktax.demo.domain.customer.Customer;
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
    public List<CustomerDto> customers(@RequestParam(required = false) Long cpaId) {
        List<Customer> list = (cpaId == null)
                ? customerRepository.findAll()
                : customerRepository.findByTaxCompany_CpaId(cpaId);

        return list.stream().map(CustomerDto::new).toList();
    }

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
