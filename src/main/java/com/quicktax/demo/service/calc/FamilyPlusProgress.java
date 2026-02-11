package com.quicktax.demo.service.calc;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class FamilyPlusProgress {

    private final FamilyPlusCalculate familyPlusCalculate;

    public FamilyPlusFinalResult execute(
            int case_year,
            long tax_base_amount_input,
            long adjusted_income_amount,
            long earned_income_money,
            BigDecimal total_salary,
            BigDecimal monthly_rent_tax_credit_amount,
            long donation_total_amount,
            long eligible_children_count,
            long childbirth_adoption_count,
            BigDecimal determined_tax_amount_origin,
            long final_fee_percent,
            LocalDate[] case_work_start,
            LocalDate[] case_work_end,
            BigDecimal[] case_work_salary
    ) {

        /* ========== 1. 공통 선행 작업 ========== */
        long donationException = familyPlusCalculate.donationException(
                donation_total_amount, eligible_children_count, childbirth_adoption_count
        );
        BigDecimal donationExcepBD = BigDecimal.valueOf(donationException);
        BigDecimal earnedIncomeLimit = familyPlusCalculate.earnedIncomeLimit(total_salary);

        /* ========== 2. 일반 계산 트랙 (Normal) ========== */
        long tax_base_normal = familyPlusCalculate.tax_base_amount(tax_base_amount_input);
        FamilyPlusCalculate.TaxResult normalTax = familyPlusCalculate.calculated_tax(case_year, tax_base_normal);

        BigDecimal calc_tax_normal = normalTax.getTax();
        long tax_rate_normal = normalTax.getRate(); // 산출세율 추출

        BigDecimal reduction_normal = familyPlusCalculate.double_tax_reduction_amount(
                case_year, calc_tax_normal, total_salary, case_work_start, case_work_end, case_work_salary
        );
        BigDecimal earned_inc_normal = familyPlusCalculate.earned_income_amount(
                calc_tax_normal, earnedIncomeLimit, reduction_normal
        );
        BigDecimal total_credit_normal = familyPlusCalculate.total_tax_credit_amount(
                earned_inc_normal, monthly_rent_tax_credit_amount, calc_tax_normal, reduction_normal, donationExcepBD
        );
        BigDecimal determined_normal = familyPlusCalculate.determined_tax_amount(
                calc_tax_normal, total_credit_normal, reduction_normal
        );

        /* ========== 3. 보험 계산 트랙 (Insure) ========== */
        long tax_base_insure = familyPlusCalculate.tax_base_amount_insure(
                tax_base_amount_input, adjusted_income_amount, earned_income_money
        );
        FamilyPlusCalculate.TaxResult insureTax = familyPlusCalculate.calculated_tax(case_year, tax_base_insure);

        BigDecimal calc_tax_insure = insureTax.getTax();
        long tax_rate_insure = insureTax.getRate(); // 산출세율 추출

        BigDecimal reduction_insure = familyPlusCalculate.double_tax_reduction_amount(
                case_year, calc_tax_insure, total_salary, case_work_start, case_work_end, case_work_salary
        );
        BigDecimal earned_inc_insure = familyPlusCalculate.earned_income_amount(
                calc_tax_insure, earnedIncomeLimit, reduction_insure
        );
        BigDecimal total_credit_insure = familyPlusCalculate.total_tax_credit_amount_insure(
                earned_inc_insure, monthly_rent_tax_credit_amount, calc_tax_insure, reduction_insure, donationExcepBD
        );
        BigDecimal determined_insure = familyPlusCalculate.determined_tax_amount_insure(
                calc_tax_insure, total_credit_insure, reduction_insure
        );

        /* ========== 4. 최적 트랙 선택 (결정세액 낮은 쪽) ========== */
        boolean isNormal = determined_normal.compareTo(determined_insure) <= 0;

        // 최종 선택된 값들 매핑
        long final_tax_base = isNormal ? tax_base_normal : tax_base_insure;
        long final_tax_rate = isNormal ? tax_rate_normal : tax_rate_insure; // ✅ 선택된 세율 저장
        BigDecimal final_calc_tax = isNormal ? calc_tax_normal : calc_tax_insure;
        BigDecimal final_reduction = isNormal ? reduction_normal : reduction_insure;
        BigDecimal final_earned_inc = isNormal ? earned_inc_normal : earned_inc_insure;
        BigDecimal final_total_credit = isNormal ? total_credit_normal : total_credit_insure;
        BigDecimal final_determined = isNormal ? determined_normal : determined_insure;

        /* ========== 5. 후처리 및 결과 생성 ========== */
        BigDecimal diff = familyPlusCalculate.tax_difference_amount(final_determined, determined_tax_amount_origin);
        BigDecimal refund = familyPlusCalculate.refundTax(diff.longValue(), final_fee_percent);

        // ✅ 빌더를 통해 모든 데이터 반환
        return FamilyPlusFinalResult.builder()
                .tax_base_amount(final_tax_base)
                .calculated_tax(final_calc_tax)
                .calculated_tax_rate(final_tax_rate) // ✅ CalcService 에러 해결 포인트
                .youth_tax_reduction_amount(final_reduction)
                .earned_income_amount(final_earned_inc)
                .total_tax_credit_amount(final_total_credit)
                .determined_tax_amount(final_determined)
                .tax_difference_amount(diff)
                .refund_tax(refund)
                .build();
    }

    /* =====================================================
     * 결과 DTO
     * ===================================================== */
    @Getter
    @Builder // ✅ 빌더 패턴 적용
    public static class FamilyPlusFinalResult {
        private final long tax_base_amount;
        private final BigDecimal calculated_tax;
        private final Long calculated_tax_rate; // ✅ 필드 추가됨
        private final BigDecimal youth_tax_reduction_amount;
        private final BigDecimal earned_income_amount;
        private final BigDecimal total_tax_credit_amount;
        private final BigDecimal determined_tax_amount;
        private final BigDecimal tax_difference_amount;
        private final BigDecimal refund_tax;
    }
}