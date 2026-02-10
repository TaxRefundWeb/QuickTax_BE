package com.quicktax.demo.service.result;

import com.quicktax.demo.common.ApiException;
import com.quicktax.demo.common.ErrorCode;
import com.quicktax.demo.domain.calc.CaseCalcResult;
import com.quicktax.demo.domain.cases.TaxCase;
import com.quicktax.demo.dto.refund.RefundResultsResponse;
import com.quicktax.demo.repo.calc.CaseCalcResultRepository;
import com.quicktax.demo.repo.TaxCaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RefundResultService {

    private final TaxCaseRepository taxCaseRepository;
    private final CaseCalcResultRepository caseCalcResultRepository;

    public RefundResultsResponse getRefundResults(Long cpaId, Long caseId) {

        // 401 (명세)
        if (cpaId == null) {
            throw new ApiException(ErrorCode.AUTH401, "로그인이 필요합니다.");
        }

        // case 존재 확인
        TaxCase taxCase = taxCaseRepository.findById(caseId)
                .orElseThrow(() -> new ApiException(ErrorCode.COMMON404, "존재하지 않는 caseId 입니다."));

        // 403 (명세) - 권한 체크
        Long ownerCpaId = taxCase.getCustomer().getTaxCompany().getCpaId();
        if (!cpaId.equals(ownerCpaId)) {
            throw new ApiException(ErrorCode.AUTH403, "권한이 존재 하지 않습니다.");
        }

        // case_id로 모든 year/scenario 결과 조회
        List<CaseCalcResult> rows =
                caseCalcResultRepository.findAllByIdCaseIdOrderByIdCaseYearAscIdScenarioCodeAsc(caseId);

        // 404 (명세) - 계산 방식 없음
        if (rows.isEmpty()) {
            throw new ApiException(ErrorCode.COMMON404, "계산 방식이 존재 하지 않습니다.");
        }

        // year -> scenarios (조회 정렬 유지하려고 LinkedHashMap)
        Map<Integer, List<RefundResultsResponse.ScenarioResult>> byYear = new LinkedHashMap<>();

        for (CaseCalcResult r : rows) {
            Integer year = r.getId().getCaseYear();

            byYear.computeIfAbsent(year, k -> new ArrayList<>()).add(
                    new RefundResultsResponse.ScenarioResult(
                            r.getId().getScenarioCode(),
                            r.getTaxDifferenceAmount(),
                            r.getDeterminedTaxAmount(),
                            r.getTaxBaseAmount(),
                            // ⚠️ 현재 스키마 컬럼명이 calculated_tax_rate 로 되어있음.
                            // 명세의 calculated_tax(산출세액) 자리에 그대로 내려줌.
                            r.getCalculatedTaxRate(),
                            r.getEarnedIncomeAmount(),
                            r.getRefundAmount(),
                            r.getScenarioText()
                    )
            );
        }

        // yearResult 생성
        List<RefundResultsResponse.YearResult> yearResults = new ArrayList<>();
        for (Map.Entry<Integer, List<RefundResultsResponse.ScenarioResult>> e : byYear.entrySet()) {

            // 명세: year당 1~3개 (DB가 보장해야 정상인데, 방어로직 넣음)
            if (e.getValue().isEmpty() || e.getValue().size() > 3) {
                throw new ApiException(ErrorCode.COMMON500, "계산 결과 데이터가 비정상입니다.");
            }

            yearResults.add(new RefundResultsResponse.YearResult(e.getKey(), e.getValue()));
        }

        return new RefundResultsResponse(yearResults);
    }
}
