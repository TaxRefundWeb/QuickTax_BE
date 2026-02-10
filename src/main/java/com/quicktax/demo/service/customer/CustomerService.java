package com.quicktax.demo.service.customer;

import com.quicktax.demo.common.ApiException;
import com.quicktax.demo.common.ErrorCode;
import com.quicktax.demo.domain.auth.TaxCompany;
import com.quicktax.demo.domain.customer.Customer;
import com.quicktax.demo.dto.customer.*;
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

        Integer feePercent = parseFeePercentOrDefault(request.getFinalFeePercent(), 0);

        Customer customer = Customer.builder()
                .name(request.getName())
                .rrn(request.getRrn())
                .phone(normalizePhone(request.getPhone())) // ✅ phone 저장
                .address(request.getAddress())
                .bank(request.getBank())
                .bankNumber(request.getBankNumber())
                .nationalityCode(request.getNationalityCode())
                .nationalityName(request.getNationalityName())
                .finalFeePercent(feePercent)
                .taxCompany(taxCompany)
                .build();

        return customerRepository.save(customer).getCustomerId();
    }

    // 3. 고객 상세 정보 조회
    @Transactional(readOnly = true)
    public CustomerDetailResponse getCustomerDetail(Long cpaId, Long customerId) {
        Customer customer = checkCustomerOwnership(cpaId, customerId);
        return buildDetailResponse(customer);
    }

    // 4. 고객 정보 수정 (PATCH: name/rrn 제외 전부 수정 가능)
    @Transactional
    public CustomerDetailResponse updateCustomerInfo(Long cpaId, Long customerId, CustomerUpdateRequest request) {
        Customer customer = checkCustomerOwnership(cpaId, customerId);


        // ✅ 부분 수정: null이면 기존 값 유지
        String phone = request.getPhone() != null ? normalizePhone(request.getPhone()) : customer.getPhone();
        String address = request.getAddress() != null ? request.getAddress() : customer.getAddress();
        String bank = request.getBank() != null ? request.getBank() : customer.getBank();
        String bankNumber = request.getBankNumber() != null ? request.getBankNumber() : customer.getBankNumber();
        String nationalityCode = request.getNationalityCode() != null ? request.getNationalityCode() : customer.getNationalityCode();
        String nationalityName = request.getNationalityName() != null ? request.getNationalityName() : customer.getNationalityName();

        // ✅ final_fee_percent: 안 오면 유지, 오면 파싱, 이상하면 400
        Integer feePercent = customer.getFinalFeePercent();
        if (request.getFinalFeePercent() != null && !request.getFinalFeePercent().isBlank()) {
            feePercent = parseFeePercentOrThrow(request.getFinalFeePercent());
        }

        // ✅ 엔티티에 실제 반영
        customer.updateBasicInfo(
                phone,
                address,
                bank,
                bankNumber,
                nationalityCode,
                nationalityName,
                feePercent
        );

        return buildDetailResponse(customer);
    }

    public Customer checkCustomerOwnership(Long cpaId, Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ApiException(ErrorCode.COMMON404));

        if (!customer.getTaxCompany().getCpaId().equals(cpaId)) {
            throw new ApiException(ErrorCode.AUTH403);
        }
        return customer;
    }

    private CustomerDetailResponse buildDetailResponse(Customer customer) {
        return CustomerDetailResponse.builder()
                .name(customer.getName())
                .rrn(customer.getRrn())
                .phone(customer.getPhone()) // ✅ 더 이상 가짜 기본값 없음
                .address(customer.getAddress())
                .bank(customer.getBank())
                .bankNumber(customer.getBankNumber())
                .nationalityCode(customer.getNationalityCode())
                .nationalityName(customer.getNationalityName())
                .finalFeePercent(String.valueOf(customer.getFinalFeePercent()))
                .build();
    }

    private String formatBirthDate(String rrn) {
        if (rrn == null || rrn.length() < 6) return "정보없음";
        return rrn.substring(0, 2) + rrn.substring(2, 4) + rrn.substring(4, 6);
    }

    private Integer parseFeePercentOrDefault(String rawFee, int defaultValue) {
        if (rawFee == null || rawFee.isBlank()) return defaultValue;
        try {
            int v = Integer.parseInt(rawFee.replace("%", "").trim());
            if (v < 0 || v > 100) return defaultValue;
            return v;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private Integer parseFeePercentOrThrow(String rawFee) {
        try {
            int v = Integer.parseInt(rawFee.replace("%", "").trim());
            if (v < 0 || v > 100) {
                throw new ApiException(ErrorCode.BADREQ400, "final_fee_percent 범위는 0~100 입니다.");
            }
            return v;
        } catch (NumberFormatException e) {
            throw new ApiException(ErrorCode.BADREQ400, "final_fee_percent 형식이 올바르지 않습니다.");
        }
    }

    // phone 정규화: "   " -> null (삭제/비움 의도 허용), 양쪽 공백 제거
    private String normalizePhone(String phone) {
        if (phone == null) return null;
        String p = phone.trim();
        return p.isEmpty() ? null : p;
    }
}
