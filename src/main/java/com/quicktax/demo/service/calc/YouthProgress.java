package com.quicktax.demo.service.calc;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class YouthProgress {

    // 💡 [수정] 클래스명 대문자 적용 (youth_calculate -> YouthCalculate)
    // 이 부분은 다음 단계에서 만들 'YouthCalculate.java'를 참조합니다.
    private final YouthCalculate youthCalculate;

    /**
     * 청년 계산 실행
     * - 일반 계산 / 보험 계산 투웨이 실행
     * - 기부금 공제 계산 포함
     * - 결정세액(determined_tax_amount) 기준 비교
     */
    public YouthCalculate.FinalTaxResult executeYouthCalculate(

            int case_year,

            // 급여
            BigDecimal total_salary,
            long tax_base_amount,

            // 보험 계산용
            long adjusted_income_amount,
            BigDecimal earned_income_money,

            // 근무 기간
            LocalDate case_work_start,
            LocalDate case_work_end,

            // 기부금 공제 계산용
            long donation_total_amount,
            long eligible_children_count,
            long childbirth_adoption_count,

            // 기타 공제
            BigDecimal monthly_rent_tax_credit_amount,

            // 경정청구
            BigDecimal determined_tax_amount_origin,
            long final_fee_percent
    ) {

        /* =====================================================
         * 0. 공통 전처리 : 기부금 공제 계산 (1회)
         * ===================================================== */

        // 💡 [수정] 변수명/메서드 호출부의 클래스명을 대문자(YouthCalculate)로 변경
        long donation_exception =
                youthCalculate.donationException(
                        donation_total_amount,
                        eligible_children_count,
                        childbirth_adoption_count
                );

        BigDecimal donationException =
                BigDecimal.valueOf(donation_exception);

        // (주의) 아래 줄은 원본 코드에 있었으나, 사용되지 않는 변수일 수 있습니다. 로직 유지를 위해 남겨둡니다.
        BigDecimal earnedIncomeLimit =
                youthCalculate.earnedIncomeLimit(total_salary);


        /* =====================================================
         * 1. 일반 계산
         * ===================================================== */

        long normal_tax_base_amount =
                youthCalculate.tax_base_amount(tax_base_amount);

        // Inner Class 참조 수정 (youth_calculate.TaxResult -> YouthCalculate.TaxResult)
        YouthCalculate.TaxResult normalTax =
                youthCalculate.calculated_tax(
                        case_year,
                        normal_tax_base_amount
                );

        BigDecimal normal_calculated_tax = normalTax.getTax(); // 필드 접근 또는 Getter 사용 (DTO 정의에 따름)
        long normal_tax_rate = normalTax.getRate();

        BigDecimal normal_youth_tax_reduction_amount =
                youthCalculate.youth_tax_reduction_amount(
                        case_year,
                        normal_calculated_tax,
                        case_work_start,
                        case_work_end
                );

        BigDecimal normal_earned_income_limit =
                youthCalculate.earnedIncomeLimit(total_salary);

        BigDecimal normal_earned_income_amount =
                youthCalculate.earned_income_amount(
                        normal_calculated_tax,
                        normal_earned_income_limit,
                        normal_youth_tax_reduction_amount
                );

        BigDecimal normal_total_tax_credit =
                youthCalculate.total_tax_credit_amount(
                        normal_earned_income_amount,
                        monthly_rent_tax_credit_amount,
                        normal_calculated_tax,
                        normal_youth_tax_reduction_amount,
                        donationException
                );

        BigDecimal normal_determined_tax =
                youthCalculate.determined_tax_amount(
                        normal_calculated_tax,
                        normal_total_tax_credit,
                        normal_youth_tax_reduction_amount
                );

        BigDecimal normal_tax_difference =
                youthCalculate.tax_difference_amount(
                        normal_determined_tax,
                        determined_tax_amount_origin
                );

        BigDecimal normal_refund =
                youthCalculate.refundTax(
                        normal_tax_difference.longValue(),
                        final_fee_percent
                );

        YouthCalculate.FinalTaxResult normalResult =
                new YouthCalculate.FinalTaxResult(
                        normal_tax_base_amount,
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

        long insure_tax_base_amount =
                youthCalculate.tax_base_amount_insure(
                        normal_tax_base_amount,
                        adjusted_income_amount,
                        earned_income_money
                );

        YouthCalculate.TaxResult insureTax =
                youthCalculate.calculated_tax(
                        case_year,
                        insure_tax_base_amount
                );

        BigDecimal insure_calculated_tax = insureTax.getTax();
        long insure_tax_rate = insureTax.getRate();

        BigDecimal insure_youth_tax_reduction_amount =
                youthCalculate.youth_tax_reduction_amount(
                        case_year,
                        insure_calculated_tax,
                        case_work_start,
                        case_work_end
                );

        BigDecimal insure_earned_income_limit =
                youthCalculate.earnedIncomeLimit(total_salary);

        BigDecimal insure_earned_income_amount =
                youthCalculate.earned_income_amount(
                        insure_calculated_tax,
                        insure_earned_income_limit,
                        insure_youth_tax_reduction_amount
                );

        BigDecimal insure_total_tax_credit =
                youthCalculate.total_tax_credit_amount(
                        insure_earned_income_amount,
                        monthly_rent_tax_credit_amount,
                        insure_calculated_tax,
                        insure_youth_tax_reduction_amount,
                        donationException
                );

        BigDecimal insure_determined_tax =
                youthCalculate.determined_tax_amount(
                        insure_calculated_tax,
                        insure_total_tax_credit,
                        insure_youth_tax_reduction_amount
                );

        BigDecimal insure_tax_difference =
                youthCalculate.tax_difference_amount(
                        insure_determined_tax,
                        determined_tax_amount_origin
                );

        BigDecimal insure_refund =
                youthCalculate.refundTax(
                        insure_tax_difference.longValue(),
                        final_fee_percent
                );

        YouthCalculate.FinalTaxResult insureResult =
                new YouthCalculate.FinalTaxResult(
                        insure_tax_base_amount,
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
         * 3. 결정세액 비교
         * ===================================================== */

        // FinalTaxResult 내부 필드 접근 (public 필드라고 가정)
        if (normalResult.determined_tax_amount
                .compareTo(insureResult.determined_tax_amount) <= 0) {
            return normalResult;
        } else {
            return insureResult;
        }
    }
}