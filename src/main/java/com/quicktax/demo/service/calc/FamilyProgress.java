package com.quicktax.demo.service.calc;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FamilyProgress {

    private final FamilyCalculate familyCalculate;

    /**
     * 부양가족 계산 실행
     * - 일반 계산 / 보험 계산 투웨이 실행
     * - determined_tax_amount 기준으로 더 작은 결과 선택
     */
    public FamilyFinalResult executeFamilyCalculate(

            int case_year,

            // 소득
            long tax_base_amount,
            long adjusted_income_amount,
            long earned_income_money,
            BigDecimal total_salary,

            // 가족
            boolean spouse_yn,
            long child_num,
            List<FamilyCalculate.ChildInfo> children, // DTO 타입 수정

            // 공제
            BigDecimal monthly_rent_tax_credit_amount,
            BigDecimal donationException,

            // 경정청구
            BigDecimal determined_tax_amount_origin,
            long final_fee_percent
    ) {

        /* =====================================================
         * 1. 일반 계산
         * ===================================================== */

        long taxBase_normal =
                familyCalculate.tax_base_amount(
                        tax_base_amount
                );

        FamilyCalculate.TaxResult normalTax =
                familyCalculate.calculated_tax(case_year, taxBase_normal);

        // TaxResult에 @Getter가 적용되었으므로 getter 사용
        BigDecimal normal_calculated_tax = normalTax.getTax();

        BigDecimal earnedIncomeLimit =
                familyCalculate.earnedIncomeLimit(total_salary);

        BigDecimal normal_earned_income_amount =
                familyCalculate.earned_income_amount(
                        normal_calculated_tax,
                        earnedIncomeLimit
                );

        BigDecimal child_income_amount =
                familyCalculate.child_income_amount(
                        children,
                        case_year
                );

        BigDecimal normal_total_tax_credit =
                familyCalculate.total_tax_credit_amount(
                        child_income_amount,
                        normal_earned_income_amount,
                        monthly_rent_tax_credit_amount,
                        normal_calculated_tax,
                        donationException
                );

        BigDecimal normal_determined_tax =
                familyCalculate.determined_tax_amount_family(
                        normal_calculated_tax,
                        normal_total_tax_credit
                );

        BigDecimal normal_tax_difference =
                familyCalculate.tax_difference_amount(
                        normal_determined_tax,
                        determined_tax_amount_origin
                );

        BigDecimal normal_refund =
                familyCalculate.refundTax(
                        normal_tax_difference.longValue(),
                        final_fee_percent
                );

        FamilyFinalResult normalResult =
                new FamilyFinalResult(
                        normal_determined_tax,
                        normal_tax_difference,
                        normal_refund
                );

        /* =====================================================
         * 2. 보험 계산
         * ===================================================== */

        long taxBase_insure =
                familyCalculate.tax_base_amount_insure(
                        tax_base_amount,
                        adjusted_income_amount,
                        earned_income_money
                );

        FamilyCalculate.TaxResult insureTax =
                familyCalculate.calculated_tax(case_year, taxBase_insure);

        BigDecimal insure_calculated_tax = insureTax.getTax();

        BigDecimal insure_earned_income_amount =
                familyCalculate.earned_income_amount(
                        insure_calculated_tax,
                        earnedIncomeLimit
                );

        BigDecimal insure_total_tax_credit =
                familyCalculate.total_tax_credit_amount_insure(
                        child_income_amount,
                        insure_earned_income_amount,
                        monthly_rent_tax_credit_amount,
                        insure_calculated_tax,
                        donationException
                );

        // (주의) 원본 코드에서 보험 계산 시에도 determined_tax_amount_family를 호출하고 있어 그대로 유지합니다.
        // 로직: 산출세액 - 세액공제
        BigDecimal insure_determined_tax =
                familyCalculate.determined_tax_amount_family(
                        insure_calculated_tax,
                        insure_total_tax_credit
                );

        BigDecimal insure_tax_difference =
                familyCalculate.tax_difference_amount(
                        insure_determined_tax,
                        determined_tax_amount_origin
                );

        BigDecimal insure_refund =
                familyCalculate.refundTax(
                        insure_tax_difference.longValue(),
                        final_fee_percent
                );

        FamilyFinalResult insureResult =
                new FamilyFinalResult(
                        insure_determined_tax,
                        insure_tax_difference,
                        insure_refund
                );

        /* =====================================================
         * 3. 결정세액 비교 (youth와 동일)
         * ===================================================== */

        if (normalResult.getDetermined_tax_amount()
                .compareTo(insureResult.getDetermined_tax_amount()) <= 0) {
            return normalResult;
        } else {
            return insureResult;
        }
    }

    /* =====================================================
     * 결과 DTO (최종 선택 결과만 저장)
     * ===================================================== */

    @Getter
    public static class FamilyFinalResult {

        private final BigDecimal determined_tax_amount;
        private final BigDecimal tax_difference_amount;
        private final BigDecimal refund_amount;

        public FamilyFinalResult(
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