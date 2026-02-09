package com.quicktax.demo.service.customer;

import com.quicktax.demo.common.ApiException;
import com.quicktax.demo.common.ErrorCode;
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
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final TaxCompanyRepository taxCompanyRepository;

    // 1. 고객 목록 조회
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

    // 2. 신규 고객 등록
    @Transactional
    public Long createCustomer(Long cpaId, CustomerCreateRequest request) {
        TaxCompany taxCompany = taxCompanyRepository.findById(cpaId)
                .orElseThrow(() -> new ApiException(ErrorCode.BADREQ400));

        // 💡 안전한 변환 로직 (공백, % 제거 후 숫자 변환)
        int feePercent = 0;
        try {
            String rawFee = request.getFinalFeePercent();
            if (rawFee != null && !rawFee.isBlank()) {
                // "10%" -> "10"으로 변환
                feePercent = Integer.parseInt(rawFee.replace("%", "").trim());
            }
        } catch (NumberFormatException e) {
            // 숫자가 아닐 경우 0으로 처리하거나 에러 발생 (여기선 0으로 방어)
            feePercent = 0;
        }

        Customer customer = Customer.builder()
                .name(request.getName())
                .rrn(request.getRrn())
                .address(request.getAddress())
                .bank(request.getBank())
                .bankNumber(request.getBankNumber())
                .nationalityCode(request.getNationalityCode())
                .nationalityName(request.getNationalityName())
                .finalFeePercent(feePercent) // 💡 Integer 값 저장
                .taxCompany(taxCompany)
                .build();

        return customerRepository.save(customer).getCustomerId();
    }

    // 3. 고객 상세 정보 조회
    @Transactional(readOnly = true)
    public CustomerDetailResponse getCustomerDetail(Long cpaId, Long customerId) {
        Customer customer = checkCustomerOwnership(cpaId, customerId);
        return buildDetailResponse(customer, null);
    }

    // 4. 고객 정보 수정
    @Transactional
    public CustomerDetailResponse updateCustomerInfo(Long cpaId, Long customerId, CustomerUpdateRequest request) {
        Customer customer = checkCustomerOwnership(cpaId, customerId);

        // 💡 수정 시에도 Integer 변환 필요
        int feePercent = 0;
        try {
            String rawFee = request.getFinalFeePercent();
            if (rawFee != null && !rawFee.isBlank()) {
                feePercent = Integer.parseInt(rawFee.replace("%", "").trim());
            }
        } catch (NumberFormatException e) {
            feePercent = 0;
        }

        customer.updateBasicInfo(
                request.getAddress(),
                request.getBank(),
                request.getBankNumber(),
                feePercent // 💡 Integer 값 전달
        );

        return buildDetailResponse(customer, request.getPhone());
    }

    public Customer checkCustomerOwnership(Long cpaId, Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ApiException(ErrorCode.COMMON404));

        if (!customer.getTaxCompany().getCpaId().equals(cpaId)) {
            throw new ApiException(ErrorCode.AUTH403);
        }
        return customer;
    }

    private CustomerDetailResponse buildDetailResponse(Customer customer, String phone) {
        return CustomerDetailResponse.builder()
                .name(customer.getName())
                .rrn(customer.getRrn())
                .phone(phone != null ? phone : "010-0000-0000")
                .address(customer.getAddress())
                .bank(customer.getBank())
                .bankNumber(customer.getBankNumber())
                .nationalityCode(customer.getNationalityCode())
                .nationalityName(customer.getNationalityName())
                .finalFeePercent(String.valueOf(customer.getFinalFeePercent())) // Integer -> String 변환 (응답용)
                .build();
    }

    private String formatBirthDate(String rrn) {
        if (rrn == null || rrn.length() < 6) return "정보없음";
        return rrn.substring(0, 2) + rrn.substring(2, 4) + rrn.substring(4, 6);
    }
}