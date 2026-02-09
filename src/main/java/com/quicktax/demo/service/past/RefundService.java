package com.quicktax.demo.service.past;

import com.quicktax.demo.domain.calc.CaseCalcResult;
import com.quicktax.demo.domain.calc.CaseCalcResultDocument;
import com.quicktax.demo.domain.cases.TaxCase;
import com.quicktax.demo.dto.PastDataDto;
import com.quicktax.demo.dto.PastDataResponse;
import com.quicktax.demo.repo.CaseCalcResultDocumentRepository;
import com.quicktax.demo.repo.CaseCalcResultRepository;
import com.quicktax.demo.repo.TaxCaseRepository;
import com.quicktax.demo.service.customer.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RefundService {

    private final CustomerService customerService;
    private final TaxCaseRepository taxCaseRepository;
    private final CaseCalcResultRepository caseCalcResultRepository;
    private final CaseCalcResultDocumentRepository caseCalcResultDocumentRepository;

    /**
     * GET /api/customers/{customerId}/past
     * result.pastdata: (case_id, case_year) 단위 년도별 레코드 반환
     */
    @Transactional(readOnly = true)
    public PastDataResponse getCustomerPastData(Long cpaId, Long customerId) {

        // 1) 권한 체크 (403 / 404 포함)
        customerService.checkCustomerOwnership(cpaId, customerId);

        // 2) customer의 case 목록 (케이스ID 확보)
        List<TaxCase> cases = taxCaseRepository.findByCustomer_CustomerIdAndCustomer_TaxCompany_CpaId(customerId, cpaId);
        if (cases.isEmpty()) {
            return new PastDataResponse(List.of());
        }

        // 정렬(선택): claim_date 최신 우선, 없으면 뒤로
        cases.sort(Comparator.comparing(
                (TaxCase tc) -> Optional.ofNullable(tc.getClaimDate()).orElse(LocalDate.MIN)
        ).reversed().thenComparing(TaxCase::getCaseId));

        List<Long> caseIds = cases.stream().map(TaxCase::getCaseId).toList();

        // 3) 문서 URL을 (case_id, case_year) -> url 맵으로 미리 적재
        Map<Long, Map<Integer, String>> urlMapByCase = new HashMap<>();
        List<CaseCalcResultDocument> docs =
                caseCalcResultDocumentRepository.findAllByIdCaseIdInOrderByIdCaseIdAscIdCaseYearAsc(caseIds);

        for (CaseCalcResultDocument d : docs) {
            Long caseId = d.getId().getCaseId();
            Integer year = d.getId().getCaseYear();
            urlMapByCase.computeIfAbsent(caseId, k -> new HashMap<>())
                    .putIfAbsent(year, d.getUrl()); // 혹시 중복이면 첫 값 유지
        }

        // 4) 케이스별로 calc_result를 year로 그룹핑해서 년도별 레코드 생성
        List<PastDataDto> out = new ArrayList<>();

        for (TaxCase tc : cases) {
            Long caseId = tc.getCaseId();
            LocalDate claimDate = tc.getClaimDate();

            List<CaseCalcResult> results =
                    caseCalcResultRepository.findAllByIdCaseIdOrderByIdCaseYearAscIdScenarioCodeAsc(caseId);

            Map<Integer, List<CaseCalcResult>> byYear =
                    results.stream().collect(Collectors.groupingBy(r -> r.getId().getCaseYear()));

            // year 집합: 결과 year + 문서 year union
            Set<Integer> years = new HashSet<>(byYear.keySet());
            Map<Integer, String> urlByYear = urlMapByCase.getOrDefault(caseId, Map.of());
            years.addAll(urlByYear.keySet());

            // 연도 정렬: 최신 연도 우선
            List<Integer> sortedYears = years.stream()
                    .sorted(Comparator.reverseOrder())
                    .toList();

            for (Integer year : sortedYears) {
                List<CaseCalcResult> yearRows = byYear.getOrDefault(year, List.of());

                String scenarioCode = yearRows.stream()
                        .map(r -> r.getId().getScenarioCode())
                        .filter(Objects::nonNull)
                        .distinct()
                        .collect(Collectors.joining(", "));
                if (scenarioCode.isBlank()) scenarioCode = "계산 전";

                long determined = yearRows.stream()
                        .map(CaseCalcResult::getDeterminedTaxAmount)
                        .filter(Objects::nonNull)
                        .mapToLong(Long::longValue)
                        .sum();

                long refund = yearRows.stream()
                        .map(CaseCalcResult::getRefundAmount)
                        .filter(Objects::nonNull)
                        .mapToLong(Long::longValue)
                        .sum();

                String url = urlByYear.get(year); // 없으면 null

                out.add(PastDataDto.builder()
                        .caseId(caseId)
                        .caseYear(year)
                        .claimDate(claimDate)
                        .scenarioCode(scenarioCode)
                        .determinedTaxAmount(determined)
                        .refundAmount(refund)
                        .url(url)
                        .build());
            }
        }

        return new PastDataResponse(out);
    }
}
