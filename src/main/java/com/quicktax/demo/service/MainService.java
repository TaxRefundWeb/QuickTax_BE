package com.quicktax.demo.service;

import com.quicktax.demo.domain.customer.Customer;
import com.quicktax.demo.domain.auth.TaxCompany;
import com.quicktax.demo.dto.*;
import com.quicktax.demo.repo.CustomerRepository;
import com.quicktax.demo.repo.TaxCompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MainService {

    private final CustomerRepository customerRepository;
    private final TaxCompanyRepository taxCompanyRepository;

    /**
     * 1. 고객 목록 조회 (에러 발생 지점)
     */
    @Transactional(readOnly = true)
    public CustomersResponse getCustomerList(Long cpaId) {
        // 리포지토리 메서드명 확인 필수: findByTaxCompany_CpaId
        List<Customer> customers = customerRepository.findByTaxCompany_CpaId(cpaId);

        List<CustomerDto> customerDtos = customers.stream()
                .map(customer -> CustomerDto.builder()
                        .customerid(customer.getCustomerId())
                        .name(customer.getName())
                        .birthdate(customer.getRrn().substring(0, 6)) // 임시 포맷팅
                        .rrn(customer.getRrn())
                        .build())
                .collect(Collectors.toList());

        return new CustomersResponse(customerDtos);
    }

    /**
     * 2. 신규 고객 등록
     */
    @Transactional
    public Long createCustomer(Long cpaId, CustomerCreateRequest request) {
        TaxCompany taxCompany = taxCompanyRepository.findById(cpaId)
                .orElseThrow(() -> new RuntimeException("세무사 정보를 찾을 수 없습니다."));

        Customer customer = Customer.builder()
                .name(request.getName())
                .rrn(request.getRrn())
                .address(request.getAddress())
                .bank(request.getBank())
                .bankNumber(request.getBankNumber())
                .nationalityCode(request.getNationalityCode())
                .nationalityName(request.getNationalityName())
                .finalFeePercent(Integer.parseInt(request.getFinalFeePercent()))
                .taxCompany(taxCompany)
                .build();

        return customerRepository.save(customer).getCustomerId();
    }
}