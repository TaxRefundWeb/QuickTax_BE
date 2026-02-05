package com.quicktax.demo.service.result;

import com.quicktax.demo.common.ApiException;
import com.quicktax.demo.common.ErrorCode;
import com.quicktax.demo.domain.calc.CaseCalcResult;
import com.quicktax.demo.domain.customer.Customer;
import com.quicktax.demo.domain.refund.RefundCase; // 💡 RefundCase 사용
import com.quicktax.demo.dto.refundResult.RefundResultResponse;
import com.quicktax.demo.dto.refundResult.RefundResultResponse.ScenarioResult;
import com.quicktax.demo.dto.refundResult.RefundResultResponse.YearlyResult;
import com.quicktax.demo.repo.CaseCalcResultRepository;
import com.quicktax.demo.repo.RefundCaseRepository; // 💡 RefundCaseRepository 사용
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RefundResultService {

    private final RefundCaseRepository refundCaseRepository; // 💡 수정됨
    private final CaseCalcResultRepository caseCalcResultRepository;

    @Transactional(readOnly = true)
    public RefundResultResponse getCalculationResult(Long cpaId, Long caseId) {

        // 1. Case 조회 (RefundCaseRepository 사용)
        RefundCase refundCase = refundCaseRepository.findById(caseId)
                .orElseThrow(() -> new ApiException(ErrorCode.BADREQ400, "존재하지 않는 Case ID입니다."));

        // 2. 권한 검증 (403)
        Customer customer = refundCase.getCustomer();
        if (customer == null || !customer.getTaxCompany().getCpaId().equals(cpaId)) {
            throw new ApiException(ErrorCode.AUTH403, "해당 결과에 접근할 권한이 없습니다.");
        }

        // 3. DB 조회 (Flat Data)
        List<CaseCalcResult> flatResults = caseCalcResultRepository.findAllByCaseId(caseId);

        // 4. 데이터 가공: 연도별 그룹핑
        Map<Integer, List<CaseCalcResult>> groupedByYear = flatResults.stream()
                .collect(Collectors.groupingBy(result -> result.getId().getCaseYear()));

        List<YearlyResult> yearlyResults = groupedByYear.entrySet().stream()
                .sorted(Map.Entry.comparingByKey()) // 연도 오름차순
                .map(entry -> {
                    Integer year = entry.getKey();
                    List<CaseCalcResult> yearResults = entry.getValue();

                    List<ScenarioResult> scenarios = yearResults.stream()
                            .map(this::convertToScenarioDTO)
                            .collect(Collectors.toList());

                    return YearlyResult.builder()
                            .caseYear(year)
                            .scenarios(scenarios)
                            .build();
                })
                .collect(Collectors.toList());

        return RefundResultResponse.builder()
                .refundResults(yearlyResults)
                .build();
    }

    private ScenarioResult convertToScenarioDTO(CaseCalcResult entity) {
        return ScenarioResult.builder()
                .scenarioCode(entity.getId().getScenarioCode())
                .taxDifferenceAmount(entity.getTaxDifferenceAmount())
                .determinedTaxAmount(entity.getDeterminedTaxAmount())
                .taxBaseAmount(entity.getTaxBaseAmount())
                .calculatedTax(entity.getCalculatedTax()) // 💡 추가된 필드 매핑
                .earnedIncomeAmount(entity.getEarnedIncomeAmount())
                .refundAmount(entity.getRefundAmount())
                .scenarioText(entity.getScenarioText())
                .build();
    }
}