package com.quicktax.demo.service.past;

import com.quicktax.demo.domain.refund.RefundCase;
import com.quicktax.demo.dto.PastDataDto;
import com.quicktax.demo.dto.PastDataResponse;
import com.quicktax.demo.repo.RefundCaseRepository;
import com.quicktax.demo.service.customer.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RefundService {

    private final RefundCaseRepository refundCaseRepository;
    private final CustomerService customerService; // 권한 확인을 위해 주입

    /**
     * 고객 이전 환급 기록 열람
     */
    @Transactional(readOnly = true)
    public PastDataResponse getCustomerPastData(Long cpaId, Long customerId) {
        // CustomerService의 검증 로직 활용
        customerService.checkCustomerOwnership(cpaId, customerId);

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
}