package com.quicktax.demo.service;

import com.quicktax.demo.domain.customer.Customer;
import com.quicktax.demo.dto.CustomerDto;
import com.quicktax.demo.dto.CustomersResponse;
import com.quicktax.demo.repo.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MainService {

    private final CustomerRepository customerRepository;

    @Transactional(readOnly = true)
    public CustomersResponse getCustomerList(Long cpaId) {
        // 해당 세무사의 고객 리스트 조회
        List<Customer> customers = customerRepository.findByTaxCompany_CpaId(cpaId);

        // 엔티티를 DTO로 변환
        List<CustomerDto> customerDtos = customers.stream()
                .map(customer -> CustomerDto.builder()
                        .customerid(customer.getCustomerId())
                        .name(customer.getName())
                        // DB 데이터에 따라 적절히 가공 (예시: 2003-11-17 형식)
                        .birthdate(formatBirthDate(customer.getRrn()))
                        .rrn(customer.getRrn()) // 전체 주민번호 출력
                        .build())
                .collect(Collectors.toList());

        // 래퍼 클래스에 담아서 반환
        return new CustomersResponse(customerDtos);
    }

    private String formatBirthDate(String rrn) {
        if (rrn == null || rrn.length() < 6) return "정보없음";
        // 주민번호 앞 6자리를 이용해 YYYY-MM-DD 형식으로 변환하는 로직 (간이 구현)
        String yearPrefix = (rrn.charAt(7) == '3' || rrn.charAt(7) == '4') ? "20" : "19";
        return yearPrefix + rrn.substring(0, 2) + "-" + rrn.substring(2, 4) + "-" + rrn.substring(4, 6);
    }
}