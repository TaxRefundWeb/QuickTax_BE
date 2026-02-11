package com.quicktax.demo.service.calc;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MixProgress {

    private final MixCalculate mixCalculate;

    public MixFinalResult executeMixCalculate(

            int case_year,

            // 소득
            long tax_base_amount,
            long adjusted_income_amount,
            long earned_income_money,
            BigDecimal total_salary,

            // 가족
            List<MixCalculate.ChildInfo> children,

            // 청년 근무 기간
            LocalDate case_work_start,
            LocalDate case_work_end,

            // 공제
            BigDecimal monthly_rent_tax_credit_amount,
            BigDecimal donationException,

            // 경정청구
            BigDecimal determined_tax_amount_origin,
            long final_fee_percent
    ) {

        /* =====================================================
         * 공통 선행 계산
         * ===================================================== */

        BigDecimal earnedIncomeLimit =
                mixCalculate.earnedIncomeLimit(total_salary);

        // MixCalculate의 child_income_amount 메서드를 사용하여 자녀 공제액 계산
        BigDecimal child_income_amount =
                mixCalculate.child_income_amount(
                        children,
                        case_year
                );

        /* =====================================================
         * 1. 일반 계산
         * ===================================================== */

        long taxBase_normal =
                mixCalculate.tax_base_amount(tax_base_amount);

        MixCalculate.TaxResult normalTax =
                mixCalculate.calculated_tax(
                        case_year,
                        taxBase_normal
                );

        BigDecimal normal_calculated_tax = normalTax.getTax(); // Getter 사용

        BigDecimal normal_youth_reduction =
                mixCalculate.youth_tax_reduction_amount(
                        case_year,
                        normal_calculated_tax,
                        case_work_start,
                        case_work_end
                );

        BigDecimal normal_earned_income_amount =
                mixCalculate.earned_income_amount(
                        normal_calculated_tax,
                        earnedIncomeLimit,
                        normal_youth_reduction
                );

        // 메서드 인자 순서 주의: MixCalculate 정의에 맞춤
        BigDecimal normal_total_tax_credit =
                mixCalculate.total_tax_credit_amount_combined(
                        child_income_amount,
                        normal_earned_income_amount,
                        normal_youth_reduction,
                        monthly_rent_tax_credit_amount,
                        normal_calculated_tax,
                        donationException
                );

        BigDecimal normal_determined_tax =
                mixCalculate.determined_tax_amount(
                        normal_calculated_tax,
                        normal_total_tax_credit,
                        normal_youth_reduction
                );

        BigDecimal normal_tax_difference =
                mixCalculate.tax_difference_amount(
                        normal_determined_tax,
                        determined_tax_amount_origin
                );

        BigDecimal normal_refund =
                mixCalculate.refundTax(
                        normal_tax_difference.longValue(),
                        final_fee_percent
                );

        MixFinalResult normalResult =
                new MixFinalResult(
                        normal_determined_tax,
                        normal_tax_difference,
                        normal_refund
                );

        /* =====================================================
         * 2. 보험 계산
         * ===================================================== */

        long taxBase_insure =
                mixCalculate.tax_base_amount_insure(
                        tax_base_amount,
                        adjusted_income_amount,
                        earned_income_money
                );

        MixCalculate.TaxResult insureTax =
                mixCalculate.calculated_tax(
                        case_year,
                        taxBase_insure
                );

        BigDecimal insure_calculated_tax = insureTax.getTax(); // Getter 사용

        BigDecimal insure_youth_reduction =
                mixCalculate.youth_tax_reduction_amount(
                        case_year,
                        insure_calculated_tax,
                        case_work_start,
                        case_work_end
                );

        BigDecimal insure_earned_income_amount =
                mixCalculate.earned_income_amount(
                        insure_calculated_tax,
                        earnedIncomeLimit,
                        insure_youth_reduction
                );

        BigDecimal insure_total_tax_credit =
                mixCalculate.total_tax_credit_amount_combined_insure(
                        child_income_amount,
                        insure_earned_income_amount,
                        insure_youth_reduction,
                        monthly_rent_tax_credit_amount,
                        insure_calculated_tax,
                        donationException
                );

        BigDecimal insure_determined_tax =
                mixCalculate.determined_tax_amount_insure(
                        insure_calculated_tax,
                        insure_total_tax_credit,
                        insure_youth_reduction
                );

        BigDecimal insure_tax_difference =
                mixCalculate.tax_difference_amount(
                        insure_determined_tax,
                        determined_tax_amount_origin
                );

        BigDecimal insure_refund =
                mixCalculate.refundTax(
                        insure_tax_difference.longValue(),
                        final_fee_percent
                );

        MixFinalResult insureResult =
                new MixFinalResult(
                        insure_determined_tax,
                        insure_tax_difference,
                        insure_refund
                );

        /* =====================================================
         * 3. 결정세액 비교
         * ===================================================== */

        if (normalResult.getDetermined_tax_amount()
                .compareTo(insureResult.getDetermined_tax_amount()) <= 0) {
            return normalResult;
        }

        return insureResult;
    }

    /* =====================================================
     * 결과 DTO
     * ===================================================== */

    @Getter
    public static class MixFinalResult {

        private final BigDecimal determined_tax_amount;
        private final BigDecimal tax_difference_amount;
        private final BigDecimal refund_amount;

        public MixFinalResult(
                BigDecimal determined_tax_amount,
                BigDecimal tax_difference_amount,
                BigDecimal refund_amount
        ) {
            this.determined_tax_amount = determined_tax_amount;
            this.tax_difference_amount = tax_difference_amount;
            this.refund_amount = refund_amount;
        }
    }
}