package com.quicktax.demo.service.calc;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DoubleProgress {

    private final DoubleCalculate doubleCalculate;

    public DoubleCalculate.FinalTaxResult execute(

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

        /* ========= 공통 ========= */

        long donationException =
                doubleCalculate.donationException(
                        donation_total_amount,
                        eligible_children_count,
                        childbirth_adoption_count
                );

        BigDecimal earnedIncomeLimit =
                doubleCalculate.earnedIncomeLimit(total_salary);

        /* ========= 일반 계산 ========= */

        long tax_base_amount_normal =
                doubleCalculate.tax_base_amount(tax_base_amount_input);

        // TaxResult 객체로 반환되므로 분해해서 사용
        DoubleCalculate.TaxResult normalTaxResult =
                doubleCalculate.calculated_tax(
                        case_year,
                        tax_base_amount_normal
                );
        BigDecimal calculated_tax_normal = normalTaxResult.getTax();
        long normal_tax_rate = normalTaxResult.getRate();

        BigDecimal youth_tax_reduction_amount_normal =
                doubleCalculate.double_tax_reduction_amount(
                        case_year,
                        calculated_tax_normal,
                        total_salary,
                        case_work_start,
                        case_work_end,
                        case_work_salary
                );

        BigDecimal earned_income_amount_normal =
                doubleCalculate.earned_income_amount(
                        calculated_tax_normal,
                        earnedIncomeLimit,
                        youth_tax_reduction_amount_normal
                );

        BigDecimal total_tax_credit_amount_normal =
                doubleCalculate.total_tax_credit_amount(
                        earned_income_amount_normal,
                        monthly_rent_tax_credit_amount,
                        calculated_tax_normal,
                        youth_tax_reduction_amount_normal,
                        BigDecimal.valueOf(donationException)
                );

        BigDecimal determined_tax_amount_normal =
                doubleCalculate.determined_tax_amount(
                        calculated_tax_normal,
                        total_tax_credit_amount_normal,
                        youth_tax_reduction_amount_normal
                );

        /* ========= 보험 계산 ========= */

        long tax_base_amount_insure =
                doubleCalculate.tax_base_amount_insure(
                        tax_base_amount_input,
                        adjusted_income_amount,
                        BigDecimal.valueOf(earned_income_money) // Long -> BigDecimal 변환
                );

        DoubleCalculate.TaxResult insureTaxResult =
                doubleCalculate.calculated_tax(
                        case_year,
                        tax_base_amount_insure
                );
        BigDecimal calculated_tax_insure = insureTaxResult.getTax();
        long insure_tax_rate = insureTaxResult.getRate();

        BigDecimal youth_tax_reduction_amount_insure =
                doubleCalculate.double_tax_reduction_amount(
                        case_year,
                        calculated_tax_insure,
                        total_salary,
                        case_work_start,
                        case_work_end,
                        case_work_salary
                );

        BigDecimal earned_income_amount_insure =
                doubleCalculate.earned_income_amount(
                        calculated_tax_insure,
                        earnedIncomeLimit,
                        youth_tax_reduction_amount_insure
                );

        BigDecimal total_tax_credit_amount_insure =
                doubleCalculate.total_tax_credit_amount_insure(
                        earned_income_amount_insure,
                        monthly_rent_tax_credit_amount,
                        calculated_tax_insure,
                        youth_tax_reduction_amount_insure,
                        BigDecimal.valueOf(donationException)
                );

        BigDecimal determined_tax_amount_insure =
                doubleCalculate.determined_tax_amount_insure(
                        calculated_tax_insure,
                        total_tax_credit_amount_insure,
                        youth_tax_reduction_amount_insure
                );

        /* ========= 트랙 선택 ========= */

        boolean isNormal =
                determined_tax_amount_normal
                        .compareTo(determined_tax_amount_insure) <= 0;

        long final_tax_base_amount =
                isNormal ? tax_base_amount_normal : tax_base_amount_insure;

        BigDecimal final_calculated_tax =
                isNormal ? calculated_tax_normal : calculated_tax_insure;

        long final_tax_rate =
                isNormal ? normal_tax_rate : insure_tax_rate;

        BigDecimal final_youth_tax_reduction_amount =
                isNormal
                        ? youth_tax_reduction_amount_normal
                        : youth_tax_reduction_amount_insure;

        BigDecimal final_earned_income_amount =
                isNormal
                        ? earned_income_amount_normal
                        : earned_income_amount_insure;

        BigDecimal final_total_tax_credit_amount =
                isNormal
                        ? total_tax_credit_amount_normal
                        : total_tax_credit_amount_insure;

        BigDecimal final_determined_tax_amount =
                isNormal
                        ? determined_tax_amount_normal
                        : determined_tax_amount_insure;

        /* ========= 후처리 ========= */

        BigDecimal tax_difference_amount =
                doubleCalculate.tax_difference_amount(
                        final_determined_tax_amount,
                        determined_tax_amount_origin
                );

        BigDecimal refund_tax =
                doubleCalculate.refundTax(
                        tax_difference_amount.longValue(),
                        final_fee_percent
                );

        // 결과 객체 생성 및 반환
        return new DoubleCalculate.FinalTaxResult(
                final_tax_base_amount,
                final_calculated_tax,
                final_tax_rate,
                final_earned_income_amount,
                final_youth_tax_reduction_amount,
                final_total_tax_credit_amount,
                final_determined_tax_amount,
                refund_tax,
                tax_difference_amount
        );
    }
}