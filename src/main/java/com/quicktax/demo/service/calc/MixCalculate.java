package com.quicktax.demo.service.calc;

import lombok.Getter;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class MixCalculate {

    /* 기부금 공제 */
    /* 계(64): donation_total_amount, 자녀-공제대상자녀: eligible_children_count, 출산-입양자: childbirth_adoption_count */
    public long donationException(long donation_total_amount, long eligible_children_count, long childbirth_adoption_count) {
        return donation_total_amount - eligible_children_count - childbirth_adoption_count;
    }

    /* 종합소득 과세표준 */
    public long tax_base_amount(long tax_base_amount) {
        return tax_base_amount;
    }

    /* 보험 포함 (부양) */
    public long tax_base_amount_insure(
            long tax_base_amount,
            long adjusted_income_amount,
            long earned_income_money) {

        long insurance_amount =
                adjusted_income_amount
                        - 1_500_000L
                        - earned_income_money;

        return tax_base_amount - insurance_amount;
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

    // 근로소득 세액공제 한도
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

    // 근로소득 세액공제
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
        BigDecimal reductionRatio = youth_tax_reduction_amount.divide(calculated_tax, 10, RoundingMode.HALF_UP);

        return capped.multiply(BigDecimal.ONE.subtract(reductionRatio)).setScale(0, RoundingMode.DOWN);
    }

    // 자녀 세액 공제
    @Getter
    public static class ChildInfo {
        String residentNumber;
        long birthOrder;

        public ChildInfo(String residentNumber, long birthOrder) {
            this.residentNumber = residentNumber;
            this.birthOrder = birthOrder;
        }
    }

    private LocalDate parseBirthDate(String residentNumber) {
        String[] parts = residentNumber.split("-");
        String yymmdd = parts[0];
        char genderCode = parts[1].charAt(0);
        long year = Long.parseLong(yymmdd.substring(0, 2));
        long month = Long.parseLong(yymmdd.substring(2, 4));
        long day = Long.parseLong(yymmdd.substring(4, 6));

        if (genderCode == '1' || genderCode == '2') {
            year += 1900;
        } else {
            year += 2000;
        }
        return LocalDate.of((int) year, (int) month, (int) day);
    }

    private long calculateAge(LocalDate birthDate, long reductionYear) {
        LocalDate standardDate = LocalDate.of((int) reductionYear, 12, 31);
        long age = standardDate.getYear() - birthDate.getYear();
        if (birthDate.plusYears(age).isAfter(standardDate)) {
            age--;
        }
        return age;
    }

    public BigDecimal child_income_amount(List<ChildInfo> children, long reductionYear) {
        if (children == null || children.isEmpty()) return BigDecimal.ZERO;

        long eligibleChildCount = 0;
        BigDecimal birthReduction = BigDecimal.ZERO;

        for (ChildInfo child : children) {
            if (child.residentNumber == null) continue;
            LocalDate birthDate = parseBirthDate(child.residentNumber);
            long age = calculateAge(birthDate, reductionYear);

            boolean isEligible = (reductionYear == 2024 && age >= 8) || (reductionYear != 2024 && age >= 7);
            if (isEligible) eligibleChildCount++;

            if (birthDate.getYear() == reductionYear) {
                if (child.birthOrder == 1) birthReduction = birthReduction.add(BigDecimal.valueOf(300_000));
                else if (child.birthOrder == 2) birthReduction = birthReduction.add(BigDecimal.valueOf(500_000));
                else birthReduction = birthReduction.add(BigDecimal.valueOf(700_000));
            }
        }

        BigDecimal basicReduction;
        if (eligibleChildCount == 0) basicReduction = BigDecimal.ZERO;
        else if (eligibleChildCount == 1) basicReduction = BigDecimal.valueOf(150_000);
        else if (eligibleChildCount == 2) basicReduction = BigDecimal.valueOf(350_000);
        else basicReduction = BigDecimal.valueOf(eligibleChildCount * 300_000L - 250_000);

        return basicReduction.add(birthReduction);
    }

    // 세액공제 계 (청년+자녀)
    public BigDecimal total_tax_credit_amount_combined(
            BigDecimal child_income_amount,
            BigDecimal earned_income_amount,
            BigDecimal youth_tax_reduction_amount,
            BigDecimal monthly_rent_tax_credit_amount,
            BigDecimal calculated_tax,
            BigDecimal donationException) {

        BigDecimal base_tax = calculated_tax.subtract(youth_tax_reduction_amount);
        BigDecimal deduction_sum = earned_income_amount.add(child_income_amount)
                .add(monthly_rent_tax_credit_amount).add(donationException);

        BigDecimal calculated_value = base_tax.subtract(deduction_sum);
        if (calculated_value.compareTo(BigDecimal.ZERO) >= 0) return calculated_value;

        return base_tax; // 0보다 작으면 base_tax 반환? (로직 검토 필요하지만 원본 유지)
    }

    public BigDecimal total_tax_credit_amount_combined_insure(
            BigDecimal child_income_amount,
            BigDecimal earned_income_amount,
            BigDecimal youth_tax_reduction_amount,
            BigDecimal monthly_rent_tax_credit_amount,
            BigDecimal calculated_tax,
            BigDecimal donationException) {

        BigDecimal insurance_tax_credit = BigDecimal.valueOf(130_000);
        BigDecimal base_tax = calculated_tax.subtract(youth_tax_reduction_amount);
        BigDecimal deduction_sum = earned_income_amount.add(child_income_amount)
                .add(monthly_rent_tax_credit_amount).add(donationException).add(insurance_tax_credit);

        BigDecimal calculated_value = base_tax.subtract(deduction_sum);
        if (calculated_value.compareTo(BigDecimal.ZERO) >= 0) return calculated_value;

        return base_tax;
    }

    // 결정세액
    public BigDecimal determined_tax_amount(BigDecimal calculated_tax, BigDecimal total_tax_credit_amount, BigDecimal youth_tax_reduction_amount) {
        return calculated_tax.subtract(total_tax_credit_amount).subtract(youth_tax_reduction_amount);
    }

    public BigDecimal determined_tax_amount_insure(BigDecimal calculated_tax, BigDecimal total_tax_credit_amount, BigDecimal youth_tax_reduction_amount) {
        return calculated_tax.subtract(total_tax_credit_amount).subtract(youth_tax_reduction_amount);
    }

    // 경정청구 전후 차액
    public BigDecimal tax_difference_amount(BigDecimal determined_tax_amount, BigDecimal determined_tax_amount_origin) {
        return determined_tax_amount_origin.subtract(determined_tax_amount);
    }

    // 환급금
    public BigDecimal refundTax(long tax_difference_amount, long final_fee_percent) {
        return bd(tax_difference_amount).multiply(bd(final_fee_percent)).divide(bd(100), 0, RoundingMode.DOWN);
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
    public static class FinalTaxResult {
        public final long tax_base_amount;
        public final BigDecimal calculated_tax;
        public final long calculated_tax_rate;
        public final BigDecimal earned_income_amount;
        public final BigDecimal youth_tax_reduction_amount;
        public final BigDecimal total_tax_credit_amount;
        public final BigDecimal determined_tax_amount;
        public final BigDecimal refund_amount;
        public final BigDecimal tax_difference_amount;

        public FinalTaxResult(long tax_base_amount, BigDecimal calculated_tax, long calculated_tax_rate,
                              BigDecimal earned_income_amount, BigDecimal youth_tax_reduction_amount,
                              BigDecimal total_tax_credit_amount, BigDecimal determined_tax_amount,
                              BigDecimal refund_amount, BigDecimal tax_difference_amount) {
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