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
        TaxCompany taxCompany = taxCompanyRepository.findById(cpaId)
                .orElseThrow(() -> new ApiException(ErrorCode.BADREQ400));

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
     * 3. 고객 이전 기록 열람
     */
    @Transactional(readOnly = true)
    public PastDataResponse getCustomerPastData(Long cpaId, Long customerId) {
        checkCustomerOwnership(cpaId, customerId);

        List<RefundCase> refundCases = refundCaseRepository.findByCustomer_CustomerId(customerId);

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

    /**
     * 4. 고객 기본 정보 조회
     * - 타입 오류 해결: Integer 필드를 String.valueOf()로 변환
     */
    @Transactional(readOnly = true)
    public CustomerDetailResponse getCustomerDetail(Long cpaId, Long customerId) {
        Customer customer = checkCustomerOwnership(cpaId, customerId);

        return CustomerDetailResponse.builder()
                .name(customer.getName())
                .rrn(customer.getRrn())
                .phone("010-0000-0000") // TODO: 엔티티에 phone 필드 추가 시 변경
                .address(customer.getAddress())
                .bank(customer.getBank())
                .bankNumber(customer.getBankNumber())
                .nationalityCode(customer.getNationalityCode())
                .nationalityName(customer.getNationalityName())
                .finalFeePercent(String.valueOf(customer.getFinalFeePercent())) // 💡 String으로 명시적 변환
                .build();
    }

    /**
     * 5. 고객 기본 정보 수정 및 결과 반환
     * - 타입 오류 해결: 빌더 내 finalFeePercent를 String.valueOf()로 처리
     */
    @Transactional
    public CustomerDetailResponse updateCustomerInfo(Long cpaId, Long customerId, CustomerUpdateRequest request) {
        Customer customer = checkCustomerOwnership(cpaId, customerId);

        // 엔티티 수정 (String -> Integer 변환 적용)
        customer.updateBasicInfo(
                request.getAddress(),
                request.getBank(),
                request.getBankNumber(),
                Integer.parseInt(request.getFinalFeePercent())
        );

        // 수정된 결과를 다시 DTO 규격(모두 String)에 맞춰 반환
        return CustomerDetailResponse.builder()
                .name(customer.getName())
                .rrn(customer.getRrn())
                .phone(request.getPhone())
                .address(customer.getAddress())
                .bank(customer.getBank())
                .bankNumber(customer.getBankNumber())
                .nationalityCode(customer.getNationalityCode())
                .nationalityName(customer.getNationalityName())
                .finalFeePercent(String.valueOf(customer.getFinalFeePercent())) // 💡 String으로 명시적 변환
                .build();
    }

    /**
     * [공통 로직] 고객 존재 여부 및 세무사 권한 검증
     */
    private Customer checkCustomerOwnership(Long cpaId, Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ApiException(ErrorCode.COMMON404));

        if (!customer.getTaxCompany().getCpaId().equals(cpaId)) {
            throw new ApiException(ErrorCode.AUTH403);
        }
        return customer;
    }

    private String formatBirthDate(String rrn) {
        if (rrn == null || rrn.length() < 6) return "정보없음";
        return rrn.substring(0, 2) + rrn.substring(2, 4) + rrn.substring(4, 6);
    }
}