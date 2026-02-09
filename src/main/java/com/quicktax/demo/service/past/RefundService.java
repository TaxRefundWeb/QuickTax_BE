package com.quicktax.demo.service.past;

import com.quicktax.demo.domain.calc.CaseCalcResult; // 💡 Import 추가
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
    private final CustomerService customerService;

    /**
     * 고객 이전 환급 기록 열람
     */
    @Transactional(readOnly = true)
    public PastDataResponse getCustomerPastData(Long cpaId, Long customerId) {
        // 1. 권한 확인
        customerService.checkCustomerOwnership(cpaId, customerId);

        // 2. 해당 고객의 모든 환급 건 조회
        List<RefundCase> refundCases = refundCaseRepository.findByCustomer_CustomerId(customerId);

        // 3. DTO 변환 (CaseCalcResult 데이터를 집계해서 넣어야 함)
        List<PastDataDto> pastDataList = refundCases.stream()
                .map(refundCase -> {

                    // 연결된 계산 결과 리스트 가져오기
                    List<CaseCalcResult> results = refundCase.getResults();

                    // (1) 시나리오 코드: 여러 개일 수 있으므로 콤마(,)로 연결 (예: "청년, 자녀")
                    String scenarioCodes = results.stream()
                            .map(r -> r.getId().getScenarioCode()) // ID 안에 있음
                            .distinct()
                            .collect(Collectors.joining(", "));

                    // (2) 결정세액 합계 계산
                    Long totalDeterminedTax = results.stream()
                            .mapToLong(r -> r.getDeterminedTaxAmount() != null ? r.getDeterminedTaxAmount() : 0L)
                            .sum();

                    // (3) 환급액 합계 계산
                    Long totalRefund = results.stream()
                            .mapToLong(r -> r.getRefundAmount() != null ? r.getRefundAmount() : 0L)
                            .sum();

                    return PastDataDto.builder()
                            .caseId(refundCase.getCaseId())
                            .caseDate(refundCase.getCaseDate().toString())
                            .scenarioCode(scenarioCodes.isEmpty() ? "계산 전" : scenarioCodes) // 결과가 없으면 '계산 전' 표시
                            .determinedTaxAmount(totalDeterminedTax)
                            .refundAmount(totalRefund)
                            .build();
                })
                .collect(Collectors.toList());

        return new PastDataResponse(pastDataList);
    }
}