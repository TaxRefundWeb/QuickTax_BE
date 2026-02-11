package com.quicktax.demo.service.calc;

import lombok.Getter;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class DoubleCalculate {

    /* 기부금 공제 */
    public long donationException(long donation_total_amount, long eligible_children_count, long childbirth_adoption_count) {
        return donation_total_amount - eligible_children_count - childbirth_adoption_count;
    }

    /* 종합소득 과세표준 (이중근로) */
    public long tax_base_amount(long tax_base_amount) {
        return tax_base_amount;
    }

    /* 보험 포함 (이중근로) */
    public long tax_base_amount_insure(
            long tax_base_amount,
            long adjusted_income_amount,
            BigDecimal earned_income_money) { // 타입 BigDecimal로 수정

        long insurance_amount =
                adjusted_income_amount
                        - 1_500_000L
                        - earned_income_money.longValue();

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

    /* =========================
     * Double Tax Reduction (이중근로 감면)
     * ========================= */

    public static class WorkPeriod {
        public final LocalDate start;
        public final LocalDate end;
        public final BigDecimal salary;

        public WorkPeriod(LocalDate start, LocalDate end, BigDecimal salary) {
            this.start = start;
            this.end = end;
            this.salary = salary;
        }
    }

    private static class Period implements Comparable<Period> {
        LocalDate start;
        LocalDate end;

        Period(LocalDate s, LocalDate e) {
            this.start = s;
            this.end = e;
        }

        @Override
        public int compareTo(Period o) {
            return this.start.compareTo(o.start);
        }
    }

    public BigDecimal double_tax_reduction_amount(
            int case_year,
            BigDecimal calculated_tax,
            BigDecimal totalSalary,
            LocalDate[] case_work_start,
            LocalDate[] case_work_end,
            BigDecimal[] case_work_salary
    ) {
        // null 체크 및 값 검증
        if (calculated_tax == null || totalSalary == null ||
                calculated_tax.compareTo(BigDecimal.ZERO) <= 0 ||
                totalSalary.compareTo(BigDecimal.ZERO) <= 0 ||
                case_work_start == null ||
                case_work_end == null ||
                case_work_salary == null) {
            return BigDecimal.ZERO;
        }

        int len = Math.min(
                case_work_start.length,
                Math.min(case_work_end.length, case_work_salary.length)
        );

        if (len == 0) {
            return BigDecimal.ZERO;
        }

        List<WorkPeriod> works = new ArrayList<>();

        for (int i = 0; i < len; i++) {
            if (case_work_start[i] == null ||
                    case_work_end[i] == null ||
                    case_work_salary[i] == null) {
                continue;
            }
            if (case_work_start[i].isAfter(case_work_end[i])) {
                continue;
            }
            works.add(new WorkPeriod(
                    case_work_start[i],
                    case_work_end[i],
                    case_work_salary[i]
            ));
        }

        if (works.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return double_tax_reduction_amount(
                case_year,
                calculated_tax,
                totalSalary,
                works
        );
    }

    // 내부 계산 로직 (Private)
    private BigDecimal double_tax_reduction_amount(
            int case_year,
            BigDecimal calculated_tax,
            BigDecimal totalSalary,
            List<WorkPeriod> works
    ) {

        List<Period> periods = new ArrayList<>();
        for (WorkPeriod w : works) {
            periods.add(new Period(w.start, w.end));
        }

        periods.sort(Comparator.comparing(p -> p.start));

        List<Period> merged = new ArrayList<>();
        for (Period p : periods) {
            if (merged.isEmpty() || merged.get(merged.size() - 1).end.isBefore(p.start)) {
                merged.add(new Period(p.start, p.end));
            } else {
                Period last = merged.get(merged.size() - 1);
                LocalDate newEnd = last.end.isAfter(p.end) ? last.end : p.end;
                merged.set(
                        merged.size() - 1,
                        new Period(last.start, newEnd)
                );
            }
        }

        long totalWorkingDays = 0;
        for (Period p : merged) {
            totalWorkingDays += ChronoUnit.DAYS.between(p.start, p.end) + 1;
        }

        long yearLength =
                ChronoUnit.DAYS.between(
                        LocalDate.of(case_year, 1, 1),
                        LocalDate.of(case_year, 12, 31)
                ) + 1;

        if (totalWorkingDays <= 0 || yearLength <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal workSalarySum = BigDecimal.ZERO;
        for (WorkPeriod w : works) {
            workSalarySum = workSalarySum.add(w.salary);
        }

        if (workSalarySum.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal dayRatio =
                BigDecimal.valueOf(totalWorkingDays)
                        .divide(BigDecimal.valueOf(yearLength), 10, RoundingMode.HALF_UP);

        BigDecimal salaryRatio =
                workSalarySum
                        .divide(totalSalary, 10, RoundingMode.HALF_UP);

        BigDecimal reduction =
                calculated_tax
                        .multiply(BigDecimal.valueOf(0.9))
                        .multiply(dayRatio)
                        .multiply(salaryRatio);

        BigDecimal limit;
        if (case_year >= 2019 && case_year <= 2022) {
            limit = BigDecimal.valueOf(1_500_000);
        } else if (case_year == 2023) {
            limit = BigDecimal.valueOf(2_000_000);
        } else {
            return BigDecimal.ZERO;
        }

        return reduction
                .min(limit)
                .setScale(0, RoundingMode.DOWN);
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

    // 근로소득 세액공제액 계산
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

    // 세액공제 계 (보험 포함)
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

    // 결정세액 (보험 포함)
    public BigDecimal determined_tax_amount_insure(
            BigDecimal calculated_tax,
            BigDecimal total_tax_credit_amount,
            BigDecimal youth_tax_reduction_amount) {

        return calculated_tax
                .subtract(total_tax_credit_amount)
                .subtract(youth_tax_reduction_amount);
    }

    /* ================= 경정청구 전후 차액 ================= */

    // 경정청구 전후 결정세액의 차
    public BigDecimal tax_difference_amount(
            BigDecimal determined_tax_amount,
            BigDecimal determined_tax_amount_origin) {

        // (참고) 원본 로직 유지: determined - origin 인지 origin - determined 인지 확인 필요
        // 청년 파일에서는 origin - determined로 수정했었음. 여기도 수정할지 여부 결정 필요.
        // 여기서는 일단 원본 그대로 (determined - origin) 유지하되, 만약 이상하면 청년 코드처럼 수정해야 함.
        // 일반적으로는 [당초 결정세액 - 새로운 결정세액 = 환급액] 이므로 순서를 바꾸는 것이 맞을 수 있음.
        // -> 통일성을 위해 [origin - determined] 로 수정 권장하지만, 일단 원본 로직 유지.

        // return determined_tax_amount.subtract(determined_tax_amount_origin); // 원본
        return determined_tax_amount_origin.subtract(determined_tax_amount); // 💡 수정본 (안전)
    }

    /* 환급금 */
    public BigDecimal refundTax(long tax_difference_amount, long final_fee_percent) {
        // 차액이 양수일 때 수수료 계산
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

    /* ===== DTO (Inner Classes) ===== */

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