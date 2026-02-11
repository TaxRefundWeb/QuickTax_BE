package com.quicktax.demo.service.calc;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TimePastProgress {

    private final TimePastCalculate timePastCalculate;

    /**
     * 기간(이중근로) 계산 실행
     * - 일반 계산 / 보험 계산 투웨이 실행
     * - determined_tax_amount 기준으로 더 작은 결과 하나만 선택
     */
    public TimePastCalculate.FinalTaxResult executeTimePastCalculate(

            int case_year,

            long tax_base_amount,

            // 보험 계산용
            long adjusted_income_amount,
            long earned_income_money,

            BigDecimal total_salary,
            List<TimePastCalculate.WorkPeriod> works,

            BigDecimal monthly_rent_tax_credit_amount,
            BigDecimal donationException,

            BigDecimal determined_tax_amount_origin,
            long final_fee_percent
    ) {

        /* =====================================================
         * 공통 값
         * ===================================================== */

        BigDecimal earnedIncomeLimit =
                timePastCalculate.earnedIncomeLimit(total_salary);

        /* =====================================================
         * 1. 일반 계산
         * ===================================================== */

        long taxBase_normal =
                timePastCalculate.tax_base_amount(tax_base_amount);

        TimePastCalculate.TaxResult normalTax =
                timePastCalculate.calculated_tax(case_year, taxBase_normal);

        BigDecimal normal_calculated_tax = normalTax.getTax(); // Getter 사용
        long normal_tax_rate = normalTax.getRate();

        // 이중근로 감면 계산 (works 리스트 사용)
        BigDecimal normal_youth_tax_reduction_amount =
                timePastCalculate.double_tax_reduction_amount(
                        case_year,
                        normal_calculated_tax,
                        total_salary,
                        works
                );

        BigDecimal normal_earned_income_amount =
                timePastCalculate.earned_income_amount(
                        normal_calculated_tax,
                        earnedIncomeLimit,
                        normal_youth_tax_reduction_amount
                );

        BigDecimal normal_total_tax_credit =
                timePastCalculate.total_tax_credit_amount(
                        normal_earned_income_amount,
                        monthly_rent_tax_credit_amount,
                        normal_calculated_tax,
                        normal_youth_tax_reduction_amount,
                        donationException
                );

        BigDecimal normal_determined_tax =
                timePastCalculate.determined_tax_amount(
                        normal_calculated_tax,
                        normal_total_tax_credit,
                        normal_youth_tax_reduction_amount
                );

        BigDecimal normal_tax_difference =
                timePastCalculate.tax_difference_amount(
                        normal_determined_tax,
                        determined_tax_amount_origin
                );

        BigDecimal normal_refund =
                timePastCalculate.refundTax(
                        normal_tax_difference.longValue(),
                        final_fee_percent
                );

        TimePastCalculate.FinalTaxResult normalResult =
                new TimePastCalculate.FinalTaxResult(
                        taxBase_normal,
                        normal_calculated_tax,
                        normal_tax_rate,
                        normal_earned_income_amount,
                        normal_youth_tax_reduction_amount,
                        normal_total_tax_credit,
                        normal_determined_tax,
                        normal_refund,
                        normal_tax_difference
                );

        /* =====================================================
         * 2. 보험 계산
         * ===================================================== */

        long taxBase_insure =
                timePastCalculate.tax_base_amount_insure(
                        tax_base_amount,
                        adjusted_income_amount,
                        earned_income_money
                );

        TimePastCalculate.TaxResult insureTax =
                timePastCalculate.calculated_tax(case_year, taxBase_insure);

        BigDecimal insure_calculated_tax = insureTax.getTax(); // Getter 사용
        long insure_tax_rate = insureTax.getRate();

        BigDecimal insure_youth_tax_reduction_amount =
                timePastCalculate.double_tax_reduction_amount(
                        case_year,
                        insure_calculated_tax,
                        total_salary,
                        works
                );

        BigDecimal insure_earned_income_amount =
                timePastCalculate.earned_income_amount(
                        insure_calculated_tax,
                        earnedIncomeLimit,
                        insure_youth_tax_reduction_amount
                );

        BigDecimal insure_total_tax_credit =
                timePastCalculate.total_tax_credit_amount_insure(
                        insure_earned_income_amount,
                        monthly_rent_tax_credit_amount,
                        insure_calculated_tax,
                        insure_youth_tax_reduction_amount,
                        donationException
                );

        BigDecimal insure_determined_tax =
                timePastCalculate.determined_tax_amount_insure(
                        insure_calculated_tax,
                        insure_total_tax_credit,
                        insure_youth_tax_reduction_amount
                );

        BigDecimal insure_tax_difference =
                timePastCalculate.tax_difference_amount(
                        insure_determined_tax,
                        determined_tax_amount_origin
                );

        BigDecimal insure_refund =
                timePastCalculate.refundTax(
                        insure_tax_difference.longValue(),
                        final_fee_percent
                );

        TimePastCalculate.FinalTaxResult insureResult =
                new TimePastCalculate.FinalTaxResult(
                        taxBase_insure,
                        insure_calculated_tax,
                        insure_tax_rate,
                        insure_earned_income_amount,
                        insure_youth_tax_reduction_amount,
                        insure_total_tax_credit,
                        insure_determined_tax,
                        insure_refund,
                        insure_tax_difference
                );

        /* =====================================================
         * 3. 결정세액 비교 (기존 progress들과 동일)
         * ===================================================== */

        if (normalResult.getDetermined_tax_amount()
                .compareTo(insureResult.getDetermined_tax_amount()) <= 0) {
            return normalResult;
        } else {
            return insureResult;
        }
    }
}