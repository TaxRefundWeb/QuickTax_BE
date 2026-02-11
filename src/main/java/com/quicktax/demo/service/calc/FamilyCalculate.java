package com.quicktax.demo.service.calc;

import lombok.Getter;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class FamilyCalculate {

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

    /* ================= 근로소득 공제 ================= */

    // 연도에 따라 총급여에서 근로소득공제 한도 계산
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

    // 근로소득 세액공제 (부양가족만)
    public BigDecimal earned_income_amount(
            BigDecimal calculated_tax,
            BigDecimal earnedIncomeLimit) {

        BigDecimal earned_income_calculate;

        if (calculated_tax.compareTo(BigDecimal.valueOf(1_300_000)) <= 0) {
            earned_income_calculate = calculated_tax.multiply(BigDecimal.valueOf(0.55));
        } else {
            earned_income_calculate = calculated_tax.multiply(BigDecimal.valueOf(0.3))
                    .add(BigDecimal.valueOf(325_000));
        }

        return earned_income_calculate.min(earnedIncomeLimit).setScale(0, RoundingMode.DOWN);
    }

    /* ================= 자녀 세액 공제 ================= */

    @Getter
    public static class ChildInfo {
        String residentNumber; // "021231-1234417"
        long birthOrder;       // 1=첫째, 2=둘째, 3=셋째 이상

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

        // 1,2 → 1900년대 / 3,4 → 2000년대
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
        // 생일 안 지났으면 만 나이 -1 하는 로직인데, 기준일이 12/31이라 사실상 연 나이와 유사하게 동작
        if (birthDate.plusYears(age).isAfter(standardDate)) {
            age--;
        }
        return age;
    }

    // 자녀 세액공제 (부양)
    public BigDecimal child_income_amount(
            List<ChildInfo> children,
            long reductionYear
    ) {
        if (children == null || children.isEmpty()) {
            return BigDecimal.ZERO;
        }

        long eligibleChildCount = 0;      // 나이 기준 충족 자녀 수
        BigDecimal birthReduction = BigDecimal.ZERO; // 출생 자녀 공제

        for (ChildInfo child : children) {
            // 주민번호 → 생년월일
            if (child.getResidentNumber() == null || !child.getResidentNumber().contains("-")) {
                continue; // 잘못된 포맷 건너뜀
            }
            LocalDate birthDate = parseBirthDate(child.getResidentNumber());

            // 만 나이 계산
            long age = calculateAge(birthDate, reductionYear);

            // 연도별 나이 기준 판단
            boolean isEligible =
                    (reductionYear == 2024 && age >= 8)
                            || (reductionYear != 2024 && age >= 7);

            if (isEligible) {
                eligibleChildCount++;
            }

            // 출생 자녀 공제 (모든 연도 공통)
            if (birthDate.getYear() == reductionYear) {
                if (child.getBirthOrder() == 1) {
                    birthReduction = birthReduction.add(BigDecimal.valueOf(300_000));
                } else if (child.getBirthOrder() == 2) {
                    birthReduction = birthReduction.add(BigDecimal.valueOf(500_000));
                } else {
                    birthReduction = birthReduction.add(BigDecimal.valueOf(700_000));
                }
            }
        }

        // 기본 자녀 공제 계산
        BigDecimal basicReduction;
        if (eligibleChildCount == 0) {
            basicReduction = BigDecimal.ZERO;
        } else if (eligibleChildCount == 1) {
            basicReduction = BigDecimal.valueOf(150_000);
        } else if (eligibleChildCount == 2) {
            basicReduction = BigDecimal.valueOf(350_000);
        } else {
            basicReduction = BigDecimal.valueOf(
                    eligibleChildCount * 300_000L - 250_000
            );
        }

        // 최종 합산
        return basicReduction.add(birthReduction);
    }

    // 세액공제 계 (부양만)
    public BigDecimal total_tax_credit_amount(
            BigDecimal child_income_amount,
            BigDecimal earned_income_amount,
            BigDecimal monthly_rent_tax_credit_amount,
            BigDecimal calculated_tax,
            BigDecimal donationException) {

        if (calculated_tax.compareTo(
                earned_income_amount.add(child_income_amount)
                        .add(monthly_rent_tax_credit_amount)
                        .add(donationException)) <= 0) {

            return calculated_tax.subtract(donationException);
        }

        return child_income_amount
                .add(earned_income_amount)
                .add(monthly_rent_tax_credit_amount)
                .add(donationException);
    }

    // 세액공제 계 (부양 + 보험)
    public BigDecimal total_tax_credit_amount_insure(
            BigDecimal child_income_amount,
            BigDecimal earned_income_amount,
            BigDecimal monthly_rent_tax_credit_amount,
            BigDecimal calculated_tax,
            BigDecimal donationException) {

        BigDecimal insurance_tax_credit = BigDecimal.valueOf(130_000);

        // 산출세액 ≤ (근로 + 자녀 + 월세 + 기부금 + 보험)
        if (calculated_tax.compareTo(
                earned_income_amount
                        .add(child_income_amount)
                        .add(monthly_rent_tax_credit_amount)
                        .add(donationException)
                        .add(insurance_tax_credit)
        ) <= 0) {

            // 산출세액 − 기부금
            return calculated_tax.subtract(donationException);
        }

        // (근로 + 자녀 + 월세 + 기부금 + 보험)
        return child_income_amount
                .add(earned_income_amount)
                .add(monthly_rent_tax_credit_amount)
                .add(donationException)
                .add(insurance_tax_credit);
    }

    /* ================= 결정세액 ================= */

    // 결정세액 (부양만)
    public BigDecimal determined_tax_amount_family(
            BigDecimal calculated_tax,
            BigDecimal total_tax_credit_amount) {

        return calculated_tax.subtract(total_tax_credit_amount);
    }

    public BigDecimal determined_tax_amount_insure(
            BigDecimal calculated_tax,
            BigDecimal total_tax_credit_amount_insure) {

        return calculated_tax.subtract(total_tax_credit_amount_insure);
    }

    /* ================= 경정청구 전후 차액 ================= */

    // 경정청구 전후 결정세액의 차
    public BigDecimal tax_difference_amount(
            BigDecimal determined_tax_amount,
            BigDecimal determined_tax_amount_origin) {

        // 원본 로직 유지: [Origin - New] 순서가 환급액 계산에 유리함
        return determined_tax_amount_origin.subtract(determined_tax_amount);
    }

    /* 환급금 */
    public BigDecimal refundTax(long tax_difference_amount, long final_fee_percent) {
        // 차액이 양수일 때 수수료 적용
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
    public static class ChildTaxResult {
        private final BigDecimal total;
        private final BigDecimal birth;
        private final BigDecimal basic;
        private final BigDecimal marriage;

        public ChildTaxResult(
                BigDecimal total,
                BigDecimal birth,
                BigDecimal basic,
                BigDecimal marriage) {
            this.total = total;
            this.birth = birth;
            this.basic = basic;
            this.marriage = marriage;
        }
    }
}