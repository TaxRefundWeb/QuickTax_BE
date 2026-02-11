package com.quicktax.demo.service.calc;

import lombok.Getter;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class YouthPlusCalculate {

    /* 기부금 공제 */
    public long donationException(long donation_total_amount, long eligible_children_count, long childbirth_adoption_count) {
        return donation_total_amount - eligible_children_count - childbirth_adoption_count;
    }

    /* 종합소득 과세표준 (청년) */
    public long tax_base_amount(long tax_base_amount) {
        return tax_base_amount;
    }

    /* 보험 포함 (청년 + 가족 반영) */
    /* 차감소득금액: adjusted_income_amount, 근로소득금액: earned_income_amount */
    public long tax_base_amount_insure(
            long adjusted_income_amount,
            long earned_income_money,
            long tax_base_amount,
            boolean spouse_yn,
            boolean child_yn,
            long[] child_ids
    ) {
        long insurance_amount = adjusted_income_amount - earned_income_money;

        long familyNum = 0;
        if (spouse_yn) {
            familyNum += 1;
        }

        if (child_yn && child_ids != null) {
            familyNum += child_ids.length;
        }

        if (familyNum <= 1) {
            return tax_base_amount;
        }

        return tax_base_amount
                - (1_500_000L * familyNum)
                - insurance_amount;
    }

    /* 산출세액 */
    public TaxResult calculated_tax(int case_year, long tax_base_amount) {
        BigDecimal caculated_tax = BigDecimal.ZERO;
        int caculated_tax_rate = 0;

        if (case_year == 2023) {
            if (tax_base_amount <= 0) {
                caculated_tax = BigDecimal.ZERO;
                caculated_tax_rate = 0;
            } else if (tax_base_amount <= 14_000_000) {
                caculated_tax = bd(tax_base_amount).multiply(bd("0.06"));
                caculated_tax_rate = 6;
            } else if (tax_base_amount <= 50_000_000) {
                caculated_tax = bd(tax_base_amount).multiply(bd("0.15")).subtract(bd(1_260_000));
                caculated_tax_rate = 15;
            } else if (tax_base_amount <= 88_000_000) {
                caculated_tax = bd(tax_base_amount).multiply(bd("0.24")).subtract(bd(5_760_000));
                caculated_tax_rate = 24;
            } else if (tax_base_amount <= 150_000_000) {
                caculated_tax = bd(tax_base_amount).multiply(bd("0.35")).subtract(bd(15_440_000));
                caculated_tax_rate = 35;
            } else if (tax_base_amount <= 300_000_000) {
                caculated_tax = bd(tax_base_amount).multiply(bd("0.38")).subtract(bd(19_940_000));
                caculated_tax_rate = 38;
            } else if (tax_base_amount <= 500_000_000) {
                caculated_tax = bd(tax_base_amount).multiply(bd("0.40")).subtract(bd(25_940_000));
                caculated_tax_rate = 40;
            } else if (tax_base_amount <= 1_000_000_000) {
                caculated_tax = bd(tax_base_amount).multiply(bd("0.42")).subtract(bd(35_940_000));
                caculated_tax_rate = 42;
            } else {
                caculated_tax = bd(tax_base_amount).multiply(bd("0.45")).subtract(bd(65_940_000));
                caculated_tax_rate = 45;
            }
        } else if (case_year == 2021 || case_year == 2022) {
            if (tax_base_amount <= 0) {
                caculated_tax = BigDecimal.ZERO;
                caculated_tax_rate = 0;
            } else if (tax_base_amount <= 12_000_000) {
                caculated_tax = bd(tax_base_amount).multiply(bd("0.06"));
                caculated_tax_rate = 6;
            } else if (tax_base_amount <= 46_000_000) {
                caculated_tax = bd(tax_base_amount).multiply(bd("0.15")).subtract(bd(1_080_000));
                caculated_tax_rate = 15;
            } else if (tax_base_amount <= 88_000_000) {
                caculated_tax = bd(tax_base_amount).multiply(bd("0.24")).subtract(bd(5_220_000));
                caculated_tax_rate = 24;
            } else if (tax_base_amount <= 150_000_000) {
                caculated_tax = bd(tax_base_amount).multiply(bd("0.35")).subtract(bd(14_900_000));
                caculated_tax_rate = 35;
            } else if (tax_base_amount <= 300_000_000) {
                caculated_tax = bd(tax_base_amount).multiply(bd("0.38")).subtract(bd(19_400_000));
                caculated_tax_rate = 38;
            } else if (tax_base_amount <= 500_000_000) {
                caculated_tax = bd(tax_base_amount).multiply(bd("0.40")).subtract(bd(25_400_000));
                caculated_tax_rate = 40;
            } else if (tax_base_amount <= 1_000_000_000) {
                caculated_tax = bd(tax_base_amount).multiply(bd("0.42")).subtract(bd(35_400_000));
                caculated_tax_rate = 42;
            } else {
                caculated_tax = bd(tax_base_amount).multiply(bd("0.45")).subtract(bd(65_400_000));
                caculated_tax_rate = 45;
            }
        } else if (case_year >= 2018 && case_year <= 2020) {
            if (tax_base_amount <= 0) {
                caculated_tax = BigDecimal.ZERO;
                caculated_tax_rate = 0;
            } else if (tax_base_amount <= 12_000_000) {
                caculated_tax = bd(tax_base_amount).multiply(bd("0.06"));
                caculated_tax_rate = 6;
            } else if (tax_base_amount <= 46_000_000) {
                caculated_tax = bd(tax_base_amount).multiply(bd("0.15")).subtract(bd(1_080_000));
                caculated_tax_rate = 15;
            } else if (tax_base_amount <= 88_000_000) {
                caculated_tax = bd(tax_base_amount).multiply(bd("0.24")).subtract(bd(5_220_000));
                caculated_tax_rate = 24;
            } else if (tax_base_amount <= 150_000_000) {
                caculated_tax = bd(tax_base_amount).multiply(bd("0.35")).subtract(bd(14_900_000));
                caculated_tax_rate = 35;
            } else if (tax_base_amount <= 300_000_000) {
                caculated_tax = bd(tax_base_amount).multiply(bd("0.38")).subtract(bd(19_400_000));
                caculated_tax_rate = 38;
            } else if (tax_base_amount <= 500_000_000) {
                caculated_tax = bd(tax_base_amount).multiply(bd("0.40")).subtract(bd(25_400_000));
                caculated_tax_rate = 40;
            } else {
                caculated_tax = bd(tax_base_amount).multiply(bd("0.42")).subtract(bd(35_400_000));
                caculated_tax_rate = 42;
            }
        }

        return new TaxResult(
                caculated_tax.setScale(0, RoundingMode.DOWN),
                caculated_tax_rate
        );
    }

    // 중소기업 청년 감면
    public BigDecimal youth_tax_reduction_amount(
            int case_year,
            BigDecimal calculated_tax,
            LocalDate case_work_start,
            LocalDate case_work_end) {

        if (calculated_tax.compareTo(BigDecimal.ZERO) <= 0 ||
                case_work_start == null || case_work_end == null ||
                case_work_start.isAfter(case_work_end)) {
            return BigDecimal.ZERO;
        }

        long totalWorkingDays =
                ChronoUnit.DAYS.between(case_work_start, case_work_end) + 1;

        long yearLength =
                ChronoUnit.DAYS.between(
                        LocalDate.of(case_year, 1, 1),
                        LocalDate.of(case_year, 12, 31)
                ) + 1;

        BigDecimal ratio = BigDecimal.valueOf(totalWorkingDays)
                .divide(BigDecimal.valueOf(yearLength), 10, RoundingMode.HALF_UP);

        BigDecimal reduction = calculated_tax
                .multiply(BigDecimal.valueOf(0.9))
                .multiply(ratio);

        BigDecimal limit;
        if (case_year >= 2019 && case_year <= 2022) {
            limit = BigDecimal.valueOf(1_500_000);
        } else if (case_year == 2023) {
            limit = BigDecimal.valueOf(2_000_000);
        } else {
            return BigDecimal.ZERO;
        }

        return reduction.min(limit).setScale(0, RoundingMode.DOWN);
    }

    // 근로소득 세액공제 (청년)
    public BigDecimal earnedIncomeLimit(BigDecimal total_salary) {
        if (total_salary.compareTo(BigDecimal.valueOf(33_000_000)) <= 0) {
            return BigDecimal.valueOf(740_000);
        } else if (total_salary.compareTo(BigDecimal.valueOf(43_000_000)) <= 0) {
            return BigDecimal.valueOf(740_000)
                    .subtract(total_salary.subtract(BigDecimal.valueOf(33_000_000))
                            .multiply(BigDecimal.valueOf(0.008)))
                    .max(BigDecimal.valueOf(660_000));
        } else if (total_salary.compareTo(BigDecimal.valueOf(70_000_000)) <= 0) {
            return BigDecimal.valueOf(660_000);
        } else if (total_salary.compareTo(BigDecimal.valueOf(70_320_000)) <= 0) {
            return BigDecimal.valueOf(660_000)
                    .subtract(total_salary.subtract(BigDecimal.valueOf(70_000_000))
                            .multiply(BigDecimal.valueOf(0.5)))
                    .max(BigDecimal.valueOf(550_000));
        } else if (total_salary.compareTo(BigDecimal.valueOf(120_000_000)) <= 0) {
            return BigDecimal.valueOf(500_000);
        } else if (total_salary.compareTo(BigDecimal.valueOf(120_600_000)) <= 0) {
            return BigDecimal.valueOf(500_000)
                    .subtract(total_salary.subtract(BigDecimal.valueOf(120_000_000))
                            .multiply(BigDecimal.valueOf(0.5)))
                    .max(BigDecimal.valueOf(200_000));
        } else {
            return BigDecimal.valueOf(200_000);
        }
    }

    public BigDecimal earned_income_amount(
            BigDecimal calculated_tax,
            BigDecimal earnedIncomeLimit,
            BigDecimal youth_tax_reduction_amount) {

        if (calculated_tax.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal earned_income_calculate;
        if (calculated_tax.compareTo(BigDecimal.valueOf(1_300_000)) <= 0) {
            earned_income_calculate = calculated_tax.multiply(BigDecimal.valueOf(0.55));
        } else {
            earned_income_calculate = calculated_tax.multiply(BigDecimal.valueOf(0.3))
                    .add(BigDecimal.valueOf(325_000));
        }

        BigDecimal capped = earned_income_calculate.min(earnedIncomeLimit);

        BigDecimal reductionRatio = youth_tax_reduction_amount
                .divide(calculated_tax, 10, RoundingMode.HALF_UP);

        return capped
                .multiply(BigDecimal.ONE.subtract(reductionRatio))
                .setScale(0, RoundingMode.DOWN);
    }

    /* ================= 세액공제 계 ================= */

    // 세액공제 계 (청년만)
    public BigDecimal total_tax_credit_amount(
            BigDecimal earned_income_amount,
            BigDecimal monthly_rent_tax_credit_amount,
            BigDecimal calculated_tax,
            BigDecimal youth_tax_reduction_amount,
            BigDecimal donationException) {

        if (calculated_tax.compareTo(
                earned_income_amount
                        .add(youth_tax_reduction_amount)
                        .add(monthly_rent_tax_credit_amount)
                        .add(donationException)
        ) <= 0) {
            return calculated_tax
                    .subtract(youth_tax_reduction_amount)
                    .subtract(donationException);
        }

        return earned_income_amount
                .add(monthly_rent_tax_credit_amount)
                .add(donationException);
    }

    // 세액공제 계 (청년만)
    public BigDecimal total_tax_credit_amount_insure(
            BigDecimal earned_income_amount,
            BigDecimal monthly_rent_tax_credit_amount,
            BigDecimal calculated_tax,
            BigDecimal youth_tax_reduction_amount,
            BigDecimal donationException) {

        BigDecimal insurance_tax_credit = BigDecimal.valueOf(130_000);

        BigDecimal total_credit_base =
                earned_income_amount
                        .add(youth_tax_reduction_amount)
                        .add(monthly_rent_tax_credit_amount)
                        .add(donationException)
                        .add(insurance_tax_credit);

        if (calculated_tax.compareTo(total_credit_base) <= 0) {
            return calculated_tax
                    .subtract(youth_tax_reduction_amount)
                    .subtract(donationException);
        }

        return earned_income_amount
                .add(monthly_rent_tax_credit_amount)
                .add(donationException)
                .add(insurance_tax_credit);
    }

    // 결정세액
    public BigDecimal determined_tax_amount(
            BigDecimal calculated_tax,
            BigDecimal total_tax_credit_amount,
            BigDecimal youth_tax_reduction_amount) {

        return calculated_tax
                .subtract(total_tax_credit_amount)
                .subtract(youth_tax_reduction_amount);
    }

    public BigDecimal determined_tax_amount_insure(
            BigDecimal calculated_tax,
            BigDecimal total_tax_credit_amount_insure,
            BigDecimal youth_tax_reduction_amount) {

        return calculated_tax
                .subtract(total_tax_credit_amount_insure)
                .subtract(youth_tax_reduction_amount);
    }

    /* ================= 경정청구 전후 차액 ================= */

    // 경정청구 전후 결정세액의 차
    public BigDecimal tax_difference_amount(
            BigDecimal determined_tax_amount,
            BigDecimal determined_tax_amount_origin) {

        // 원본 로직 오류 수정: [Origin - New] 로 변경하여 환급액 계산
        return determined_tax_amount_origin.subtract(determined_tax_amount);
    }

    /* 환급금 */
    public BigDecimal refundTax(long tax_difference_amount, long final_fee_percent) {
        return bd(tax_difference_amount).multiply(bd(final_fee_percent))
                .divide(bd(100), 0, RoundingMode.DOWN);
    }

    /* ===== util ===== */
    private BigDecimal bd(long v) {
        return BigDecimal.valueOf(v);
    }

    private BigDecimal bd(String v) {
        return new BigDecimal(v);
    }

    /* ===== DTO ===== */
    @Getter
    public static class TaxResult {
        private final BigDecimal tax;
        private final long rate;

        public TaxResult(BigDecimal tax, long rate) {
            this.tax = tax;
            this.rate = rate;
        }
    }

    @Getter
    public static class DateResult {
        private final long totalWorkingDays;
        private final long yearLength;

        public DateResult(long totalWorkingDays, long yearLength) {
            this.totalWorkingDays = totalWorkingDays;
            this.yearLength = yearLength;
        }
    }

    @Getter
    public static class YouthTaxReductionResult {
        private final BigDecimal reductionAmount;
        private final BigDecimal workingRatio;
        private final long totalWorkingDays;
        private final long yearLength;

        public YouthTaxReductionResult(
                BigDecimal reductionAmount,
                BigDecimal workingRatio,
                long totalWorkingDays,
                long yearLength
        ) {
            this.reductionAmount = reductionAmount;
            this.workingRatio = workingRatio;
            this.totalWorkingDays = totalWorkingDays;
            this.yearLength = yearLength;
        }
    }

    @Getter
    public static class FinalTaxResult {
        private final long tax_base_amount;
        private final BigDecimal calculated_tax;
        private final long calculated_tax_rate;
        private final BigDecimal earned_income_amount;
        private final BigDecimal youth_tax_reduction_amount;
        private final BigDecimal total_tax_credit_amount;
        private final BigDecimal determined_tax_amount;
        private final BigDecimal refund_amount;
        private final BigDecimal tax_difference_amount;

        public FinalTaxResult(
                long tax_base_amount,
                BigDecimal calculated_tax,
                long calculated_tax_rate,
                BigDecimal earned_income_amount,
                BigDecimal youth_tax_reduction_amount,
                BigDecimal total_tax_credit_amount,
                BigDecimal determined_tax_amount,
                BigDecimal refund_amount,
                BigDecimal tax_difference_amount
        ) {
            this.tax_base_amount = tax_base_amount;
            this.calculated_tax = calculated_tax;
            this.calculated_tax_rate = calculated_tax_rate;
            this.earned_income_amount = earned_income_amount;
            this.youth_tax_reduction_amount = youth_tax_reduction_amount;
            this.total_tax_credit_amount = total_tax_credit_amount;
            this.determined_tax_amount = determined_tax_amount;
            this.refund_amount = refund_amount;
            this.tax_difference_amount = tax_difference_amount;
        }
    }
}