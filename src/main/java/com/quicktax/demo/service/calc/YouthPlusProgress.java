package com.quicktax.demo.service.calc;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class YouthPlusProgress {

    private final YouthPlusCalculate youthPlusCalculate;

    public YouthPlusResult execute(

            int case_year,

            // 소득
            long tax_base_amount,
            long adjusted_income_amount,
            long earned_income_money,
            BigDecimal total_salary,

            // 청년 근무 기간
            LocalDate case_work_start,
            LocalDate case_work_end,

            // 가족
            boolean spouse_yn,
            boolean child_yn,
            long[] child_ids,

            // 공제
            BigDecimal monthly_rent_tax_credit_amount,
            long donation_total_amount,
            long eligible_children_count,
            long childbirth_adoption_count,

            // 경정청구
            BigDecimal determined_tax_amount_origin,
            long final_fee_percent
    ) {

        /* =====================================================
         * 공통 선행 계산
         * ===================================================== */

        long donationException =
                youthPlusCalculate.donationException(
                        donation_total_amount,
                        eligible_children_count,
                        childbirth_adoption_count
                );

        BigDecimal earnedIncomeLimit =
                youthPlusCalculate.earnedIncomeLimit(total_salary);

        /* =====================================================
         * 1. 일반 계산
         * ===================================================== */

        long taxBase_normal =
                youthPlusCalculate.tax_base_amount(tax_base_amount);

        // (수정) 원본의 family_calculate -> youthPlusCalculate로 변경
        YouthPlusCalculate.TaxResult normalTax =
                youthPlusCalculate.calculated_tax(case_year, taxBase_normal);

        BigDecimal normal_calculated_tax = normalTax.getTax(); // Getter 사용

        BigDecimal normal_youth_reduction =
                youthPlusCalculate.youth_tax_reduction_amount(
                        case_year,
                        normal_calculated_tax,
                        case_work_start,
                        case_work_end
                );

        BigDecimal normal_earned_income_amount =
                youthPlusCalculate.earned_income_amount(
                        normal_calculated_tax,
                        earnedIncomeLimit,
                        normal_youth_reduction
                );

        // (주의) YouthPlusCalculate에 total_tax_credit_amount 메서드가 필요합니다.
        // YouthCalculate와 동일한 로직(청년용)을 사용한다고 가정합니다.
        BigDecimal normal_total_tax_credit =
                youthPlusCalculate.total_tax_credit_amount(
                        normal_earned_income_amount,
                        monthly_rent_tax_credit_amount,
                        normal_calculated_tax,
                        normal_youth_reduction,
                        BigDecimal.valueOf(donationException)
                );

        BigDecimal normal_determined_tax =
                youthPlusCalculate.determined_tax_amount(
                        normal_calculated_tax,
                        normal_total_tax_credit,
                        normal_youth_reduction
                );

        BigDecimal normal_tax_difference =
                youthPlusCalculate.tax_difference_amount(
                        normal_determined_tax,
                        determined_tax_amount_origin
                );

        BigDecimal normal_refund =
                youthPlusCalculate.refundTax(
                        normal_tax_difference.longValue(),
                        final_fee_percent
                );

        YouthPlusResult normalResult =
                new YouthPlusResult(
                        normal_determined_tax,
                        normal_tax_difference,
                        normal_refund
                );

        /* =====================================================
         * 2. 보험 계산
         * ===================================================== */

        long taxBase_insure =
                youthPlusCalculate.tax_base_amount_insure(
                        adjusted_income_amount,
                        earned_income_money,
                        taxBase_normal,
                        spouse_yn,
                        child_yn,
                        child_ids
                );

        // (수정) 원본의 family_calculate -> youthPlusCalculate로 변경
        YouthPlusCalculate.TaxResult insureTax =
                youthPlusCalculate.calculated_tax(case_year, taxBase_insure);

        BigDecimal insure_calculated_tax = insureTax.getTax();

        BigDecimal insure_youth_reduction =
                youthPlusCalculate.youth_tax_reduction_amount(
                        case_year,
                        insure_calculated_tax,
                        case_work_start,
                        case_work_end
                );

        BigDecimal insure_earned_income_amount =
                youthPlusCalculate.earned_income_amount(
                        insure_calculated_tax,
                        earnedIncomeLimit,
                        insure_youth_reduction
                );

        BigDecimal insure_total_tax_credit =
                youthPlusCalculate.total_tax_credit_amount_insure(
                        insure_earned_income_amount,
                        monthly_rent_tax_credit_amount,
                        insure_calculated_tax,
                        insure_youth_reduction,
                        BigDecimal.valueOf(donationException)
                );

        BigDecimal insure_determined_tax =
                youthPlusCalculate.determined_tax_amount_insure(
                        insure_calculated_tax,
                        insure_total_tax_credit,
                        insure_youth_reduction
                );

        BigDecimal insure_tax_difference =
                youthPlusCalculate.tax_difference_amount(
                        insure_determined_tax,
                        determined_tax_amount_origin
                );

        BigDecimal insure_refund =
                youthPlusCalculate.refundTax(
                        insure_tax_difference.longValue(),
                        final_fee_percent
                );

        YouthPlusResult insureResult =
                new YouthPlusResult(
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
    public static class YouthPlusResult {

        private final BigDecimal determined_tax_amount;
        private final BigDecimal tax_difference_amount;
        private final BigDecimal refund_amount;

        public YouthPlusResult(
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