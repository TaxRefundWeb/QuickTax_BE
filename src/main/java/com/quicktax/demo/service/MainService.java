package com.quicktax.demo.service;

import com.quicktax.demo.common.ApiException;
import com.quicktax.demo.common.ErrorCode;
import com.quicktax.demo.domain.customer.Customer;
import com.quicktax.demo.domain.auth.TaxCompany;
import com.quicktax.demo.domain.refund.RefundCase;
import com.quicktax.demo.dto.*;
import com.quicktax.demo.repo.CustomerRepository;
import com.quicktax.demo.repo.TaxCompanyRepository;
import com.quicktax.demo.repo.RefundCaseRepository;
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
    private final RefundCaseRepository refundCaseRepository;

    /**
     * 1. 고객 목록 조회
     */
    @Transactional(readOnly = true)
    public CustomersResponse getCustomerList(Long cpaId) {
        List<Customer> customers = customerRepository.findByTaxCompany_CpaId(cpaId);

        List<CustomerDto> customerDtos = customers.stream()
                .map(customer -> CustomerDto.builder()
                        .customerid(customer.getCustomerId())
                        .name(customer.getName())
                        .birthdate(formatBirthDate(customer.getRrn()))
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
        // 세무사 정보가 없을 경우 전역 예외 처리 규격에 맞춰 에러 던짐
        TaxCompany taxCompany = taxCompanyRepository.findById(cpaId)
                .orElseThrow(() -> new ApiException(ErrorCode.BADREQ400)); // 혹은 적절한 에러코드

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

    /**
     * 3. 고객 이전 기록 열람 (보안 검증 및 에러 처리 통합)
     */
    @Transactional(readOnly = true)
    public PastDataResponse getCustomerPastData(Long cpaId, Long customerId) {

        // [404 에러 처리] 해당 고객이 DB에 존재하지 않는 경우
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ApiException(ErrorCode.COMMON404));

        // [403 에러 처리] 고객은 존재하나, 로그인한 세무사(cpaId)가 담당자가 아닌 경우
        if (!customer.getTaxCompany().getCpaId().equals(cpaId)) {
            throw new ApiException(ErrorCode.AUTH403);
        }

        // 해당 고객의 모든 과거 환급 사례 조회
        List<RefundCase> refundCases = refundCaseRepository.findByCustomer_CustomerId(customerId);

        // DTO 변환 (동적 리스트 생성)
        List<PastDataDto> pastDataList = refundCases.stream()
                .map(refundCase -> PastDataDto.builder()
                        .caseId(refundCase.getCaseId())
                        .caseDate(refundCase.getCaseDate().toString())
                        .scenarioCode(refundCase.getScenarioCode())
                        .determinedTaxAmount(refundCase.getDeterminedTaxAmount())
                        .refundAmount(refundCase.getRefundAmount())
                        .build())
                .collect(Collectors.toList());

        return new PastDataResponse(pastDataList);
    }

    private String formatBirthDate(String rrn) {
        if (rrn == null || rrn.length() < 6) return "정보없음";
        return rrn.substring(0, 2) + rrn.substring(2, 4) + rrn.substring(4, 6);
    }
}