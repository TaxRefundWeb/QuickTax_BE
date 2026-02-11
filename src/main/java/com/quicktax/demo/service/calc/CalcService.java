package com.quicktax.demo.service.calc;

import com.quicktax.demo.common.ApiException;
import com.quicktax.demo.common.ErrorCode;
import com.quicktax.demo.domain.calc.CaseCalcResult;
import com.quicktax.demo.domain.cases.TaxCase;
import com.quicktax.demo.domain.cases.draft.CaseDraftYear;
import com.quicktax.demo.domain.cases.draft.CaseDraftYearChild;
import com.quicktax.demo.domain.cases.draft.CaseDraftYearCompany;
import com.quicktax.demo.domain.cases.draft.CaseDraftYearSpouse;
import com.quicktax.demo.domain.ocr.OcrResult;
import com.quicktax.demo.repo.TaxCaseRepository;
import com.quicktax.demo.repo.calc.CaseCalcResultRepository;
import com.quicktax.demo.repo.draft.CaseDraftYearChildRepository;
import com.quicktax.demo.repo.draft.CaseDraftYearCompanyRepository;
import com.quicktax.demo.repo.draft.CaseDraftYearRepository;
import com.quicktax.demo.repo.draft.CaseDraftYearSpouseRepository;
import com.quicktax.demo.repo.ocr.OcrResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalcService {

    private final TaxCaseRepository taxCaseRepository;
    private final OcrResultRepository ocrResultRepository;
    private final CaseCalcResultRepository resultRepository;

    private final CaseDraftYearRepository draftYearRepository;
    private final CaseDraftYearCompanyRepository draftCompanyRepository;
    private final CaseDraftYearChildRepository draftChildRepository;
    private final CaseDraftYearSpouseRepository draftSpouseRepository;

    private final CalculateDecisionService decisionService;
    private final YouthProgress youthProgress;
    private final DoubleProgress doubleProgress;
    private final MixProgress mixProgress;
    private final TimePastProgress timePastProgress;
    private final FamilyProgress familyProgress;
    private final YouthPlusProgress youthPlusProgress;
    private final FamilyPlusProgress familyPlusProgress;

    @Transactional
    public void runCalculation(Long caseId, Integer year) {

        TaxCase taxCase = taxCaseRepository.findById(caseId)
                .orElseThrow(() -> new ApiException(ErrorCode.COMMON404, "존재하지 않는 Case입니다."));

        OcrResult ocr = ocrResultRepository.findByIdCaseIdAndIdCaseYear(caseId, year)
                .orElseThrow(() -> new ApiException(ErrorCode.COMMON404, "OCR 데이터가 없습니다."));

        CaseDraftYear draftYear = draftYearRepository.findByIdCaseIdAndIdCaseYear(caseId, year)
                .orElseThrow(() -> new ApiException(ErrorCode.COMMON404, "Draft 데이터가 없습니다."));

        List<CaseDraftYearCompany> companies = draftCompanyRepository.findAllByIdCaseIdAndIdCaseYear(caseId, year);
        List<CaseDraftYearChild> children = draftChildRepository.findAllByIdCaseIdAndIdCaseYear(caseId, year);
        Optional<CaseDraftYearSpouse> spouse = draftSpouseRepository.findByIdCaseIdAndIdCaseYear(caseId, year);

        CalculationInput input = CalculationInput.builder()
                .spouseYn(draftYear.isSpouseYn())
                .childYn(draftYear.isChildYn())
                .reductionYn(draftYear.isReductionYn())
                .companyCount(companies.size())
                .basicDeductionSpouseAmount(ocr.getBasicDeductionSpouseAmount())
                .basicDeductionDependentsAmount(ocr.getBasicDeductionDependentsAmount())
                .taxReductionTotalAmount(ocr.getTaxReductionTotalAmount())
                .claimDate(taxCase.getClaimDate())
                .reductionEnd(taxCase.getReductionEnd())
                .build();

        List<ScenarioCode> scenarios = decisionService.decide(input);
        log.info("CaseId: {}, Year: {} / Scenarios: {}", caseId, year, scenarios);

        for (ScenarioCode code : scenarios) {
            processScenario(code, year, taxCase, ocr, draftYear, companies, children, spouse.isPresent());
        }
    }

    private void processScenario(
            ScenarioCode code,
            Integer year,
            TaxCase taxCase,
            OcrResult ocr,
            CaseDraftYear draft,
            List<CaseDraftYearCompany> companies,
            List<CaseDraftYearChild> children,
            boolean hasSpouse
    ) {
        long taxBase = ocr.getTaxBaseAmount() != null ? ocr.getTaxBaseAmount() : 0L;
        long adjustedIncome = ocr.getAdjustedIncomeAmount() != null ? ocr.getAdjustedIncomeAmount() : 0L;
        long earnedIncomeMoney = ocr.getEarnedIncomeAmount() != null ? ocr.getEarnedIncomeAmount() : 0L;
        BigDecimal totalSalary = BigDecimal.valueOf(ocr.getTotalSalary() != null ? ocr.getTotalSalary() : 0L);
        BigDecimal monthlyRent = BigDecimal.valueOf(ocr.getMonthlyRentTaxCreditAmount() != null ? ocr.getMonthlyRentTaxCreditAmount() : 0L);

        long donationTotal = ocr.getDonationTotalAmount() != null ? ocr.getDonationTotalAmount() : 0L;
        long eligibleChildCount = ocr.getEligibleChildrenCount() != null ? ocr.getEligibleChildrenCount() : 0L;
        long childbirthCount = ocr.getChildbirthAdoptionCount() != null ? ocr.getChildbirthAdoptionCount() : 0L;

        BigDecimal determinedTaxOrigin = BigDecimal.valueOf(ocr.getDeterminedTaxAmountOrigin() != null ? ocr.getDeterminedTaxAmountOrigin() : 0L);
        long feePercent = taxCase.getCustomer().getFinalFeePercent();

        LocalDate workStart = companies.isEmpty() ? null : companies.get(0).getCaseWorkStart();
        LocalDate workEnd = companies.isEmpty() ? null : companies.get(0).getCaseWorkEnd();

        CaseCalcResult.CaseCalcResultBuilder resultBuilder = CaseCalcResult.builder()
                .taxCase(taxCase)
                .caseYear(year)
                .scenarioCode(code.name())
                .scenarioText(getKoreanDescription(code));

        try {
            switch (code) {
                case family_reduction:
                    if (draft.isChildYn()) {
                        // 자녀 경정청구
                        List<FamilyCalculate.ChildInfo> childInfos = children.stream()
                                .map(c -> new FamilyCalculate.ChildInfo(c.getChildRrn(), 1))
                                .collect(Collectors.toList());

                        FamilyProgress.FamilyFinalResult res = familyProgress.executeFamilyCalculate(
                                year, taxBase, adjustedIncome, earnedIncomeMoney, totalSalary,
                                hasSpouse, children.size(), childInfos,
                                monthlyRent, BigDecimal.ZERO, determinedTaxOrigin, feePercent
                        );

                        resultBuilder
                                .determinedTaxAmount(res.getDetermined_tax_amount().longValue())
                                .refundAmount(res.getRefund_amount().longValue())
                                .taxDifferenceAmount(res.getTax_difference_amount().longValue());
                    } else {
                        // 청년 경정청구 (YouthCalculate.FinalTaxResult 사용)
                        YouthCalculate.FinalTaxResult res = youthProgress.executeYouthCalculate(
                                year, BigDecimal.valueOf(totalSalary.longValue()), taxBase, adjustedIncome, BigDecimal.valueOf(earnedIncomeMoney),
                                workStart, workEnd, donationTotal, eligibleChildCount, childbirthCount,
                                monthlyRent, determinedTaxOrigin, feePercent
                        );

                        // YouthCalculate.FinalTaxResult의 필드들을 매핑
                        resultBuilder
                                .taxBaseAmount(res.getTax_base_amount())
                                .calculatedTaxRate(res.getCalculated_tax_rate())
                                .earnedIncomeAmount(res.getEarned_income_amount().longValue())
                                .youthTaxReductionAmount(res.getYouth_tax_reduction_amount().longValue())
                                .totalTaxCreditAmount(res.getTotal_tax_credit_amount().longValue())
                                .determinedTaxAmount(res.getDetermined_tax_amount().longValue())
                                .refundAmount(res.getRefund_amount().longValue())
                                .taxDifferenceAmount(res.getTax_difference_amount().longValue());
                    }
                    break;

                case double_reduction:
                    // 이중근로
                    LocalDate[] starts = companies.stream().map(CaseDraftYearCompany::getCaseWorkStart).toArray(LocalDate[]::new);
                    LocalDate[] ends = companies.stream().map(CaseDraftYearCompany::getCaseWorkEnd).toArray(LocalDate[]::new);
                    BigDecimal[] salaries = new BigDecimal[companies.size()];

                    DoubleCalculate.FinalTaxResult doubleRes = doubleProgress.execute(
                            year, taxBase, adjustedIncome, earnedIncomeMoney, totalSalary,
                            monthlyRent, donationTotal, eligibleChildCount, childbirthCount,
                            determinedTaxOrigin, feePercent, starts, ends, salaries
                    );

                    resultBuilder
                            .taxBaseAmount(doubleRes.getTax_base_amount())
                            .calculatedTaxRate(doubleRes.getCalculated_tax_rate())
                            .earnedIncomeAmount(doubleRes.getEarned_income_amount().longValue())
                            .youthTaxReductionAmount(doubleRes.getYouth_tax_reduction_amount().longValue())
                            .totalTaxCreditAmount(doubleRes.getTotal_tax_credit_amount().longValue())
                            .determinedTaxAmount(doubleRes.getDetermined_tax_amount().longValue())
                            .refundAmount(doubleRes.getRefund_amount().longValue())
                            .taxDifferenceAmount(doubleRes.getTax_difference_amount().longValue());
                    break;

                case mix_reduction:
                    // 혼합
                    List<MixCalculate.ChildInfo> mixChildren = children.stream()
                            .map(c -> new MixCalculate.ChildInfo(c.getChildRrn(), 1))
                            .collect(Collectors.toList());

                    MixProgress.MixFinalResult mixRes = mixProgress.executeMixCalculate(
                            year, taxBase, adjustedIncome, earnedIncomeMoney, totalSalary,
                            mixChildren, workStart, workEnd, monthlyRent, BigDecimal.ZERO,
                            determinedTaxOrigin, feePercent
                    );

                    resultBuilder
                            .determinedTaxAmount(mixRes.getDetermined_tax_amount().longValue())
                            .refundAmount(mixRes.getRefund_amount().longValue())
                            .taxDifferenceAmount(mixRes.getTax_difference_amount().longValue());
                    break;

                case timepast_reduction:
                    // 기한 후
                    List<TimePastCalculate.WorkPeriod> tpWorks = companies.stream()
                            .map(c -> new TimePastCalculate.WorkPeriod(c.getCaseWorkStart(), c.getCaseWorkEnd(), null))
                            .collect(Collectors.toList());

                    TimePastCalculate.FinalTaxResult tpRes = timePastProgress.executeTimePastCalculate(
                            year, taxBase, adjustedIncome, earnedIncomeMoney, totalSalary,
                            tpWorks, monthlyRent, BigDecimal.ZERO, determinedTaxOrigin, feePercent
                    );

                    resultBuilder
                            .taxBaseAmount(tpRes.getTax_base_amount())
                            .calculatedTaxRate(tpRes.getCalculated_tax_rate())
                            .earnedIncomeAmount(tpRes.getEarned_income_amount().longValue())
                            .youthTaxReductionAmount(tpRes.getYouth_tax_reduction_amount().longValue())
                            .totalTaxCreditAmount(tpRes.getTotal_tax_credit_amount().longValue())
                            .determinedTaxAmount(tpRes.getDetermined_tax_amount().longValue())
                            .refundAmount(tpRes.getRefund_amount().longValue())
                            .taxDifferenceAmount(tpRes.getTax_difference_amount().longValue());
                    break;

                case youthplus_reduction:
                    // 청년 추가
                    long[] childIds = children.stream().mapToLong(c -> c.getId().getChildId()).toArray();

                    YouthPlusProgress.YouthPlusResult ypRes = youthPlusProgress.execute(
                            year, taxBase, adjustedIncome, earnedIncomeMoney, totalSalary,
                            workStart, workEnd, hasSpouse, !children.isEmpty(), childIds,
                            monthlyRent, donationTotal, eligibleChildCount, childbirthCount,
                            determinedTaxOrigin, feePercent
                    );

                    resultBuilder
                            .determinedTaxAmount(ypRes.getDetermined_tax_amount().longValue())
                            .refundAmount(ypRes.getRefund_amount().longValue())
                            .taxDifferenceAmount(ypRes.getTax_difference_amount().longValue());
                    break;

                case familyplus_reduction:
                    // 자녀 추가
                    FamilyPlusProgress.FamilyPlusFinalResult fpRes = familyPlusProgress.execute(
                            year, taxBase, adjustedIncome, earnedIncomeMoney, totalSalary,
                            monthlyRent, donationTotal, eligibleChildCount, childbirthCount,
                            determinedTaxOrigin, feePercent, null, null, null
                    );

                    resultBuilder
                            .taxBaseAmount(fpRes.getTax_base_amount())
                            .calculatedTaxRate(fpRes.getCalculated_tax_rate())
                            .earnedIncomeAmount(fpRes.getEarned_income_amount().longValue())
                            .youthTaxReductionAmount(fpRes.getYouth_tax_reduction_amount().longValue())
                            .totalTaxCreditAmount(fpRes.getTotal_tax_credit_amount().longValue())
                            .determinedTaxAmount(fpRes.getDetermined_tax_amount().longValue())
                            .refundAmount(fpRes.getRefund_tax().longValue())
                            .taxDifferenceAmount(fpRes.getTax_difference_amount().longValue());
                    break;
            }

            resultRepository.save(resultBuilder.build());

        } catch (Exception e) {
            log.error("Calculation Failed - CaseId: {}, Year: {}, Scenario: {}, Error: {}",
                    taxCase.getCaseId(), year, code, e.getMessage());
        }
    }

    private String getKoreanDescription(ScenarioCode code) {
        if (code == null) return "";
        switch (code) {
            case family_reduction: return "청년/자녀 경정청구 신청";
            case double_reduction: return "이중근로 경정청구 신청";
            case mix_reduction: return "청년+자녀 경정청구 신청";
            case timepast_reduction: return "기한 후 경정청구 신청";
            case youthplus_reduction: return "자녀 완료 후 청년 추가 신청";
            case familyplus_reduction: return "청년 완료 후 자녀 추가 신청";
            case CHILD_REDUCTION: return "자녀 경정청구 신청";
            default: return code.name();
        }
    }
}