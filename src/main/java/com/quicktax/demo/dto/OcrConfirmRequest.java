package com.quicktax.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Getter
@NoArgsConstructor
@ToString
public class OcrConfirmRequest {

    // Root 키: "OCRData"
    @JsonProperty("OCRData")
    private List<OcrYearData> ocrData;

    @Getter
    @NoArgsConstructor
    @ToString
    public static class OcrYearData {

        @JsonProperty("case_year")
        private Integer caseYear;

        @JsonProperty("total_salary")
        private Long totalSalary; // 총급여

        @JsonProperty("earned_income_deduction_amount")
        private Long earnedIncomeDeduction; // 근로소득공제

        @JsonProperty("earned_income_amount")
        private Long earnedIncomeAmount; // 근로소득금액

        @JsonProperty("basic_deduction_self_amount")
        private Long basicDeductionSelf; // 본인공제

        @JsonProperty("basic_deduction_spouse_amount")
        private Long basicDeductionSpouse; // 배우자공제

        @JsonProperty("basic_deduction_dependents_amount")
        private Long basicDeductionDependents; // 부양가족공제

        @JsonProperty("national_pension_deduction_amount")
        private Long nationalPensionDeduction; // 국민연금

        @JsonProperty("TotalSpecialIncomeDeduction") // 대소문자 주의
        private Long totalSpecialIncomeDeduction; // 특별소득공제계

        @JsonProperty("adjusted_income_amount")
        private Long adjustedIncomeAmount; // 차감소득금액

        @JsonProperty("other_income_deduction_total_amount")
        private Long otherIncomeDeductionTotal; // 그밖의 소득공제

        @JsonProperty("tax_base_amount")
        private Long taxBaseAmount; // 과세표준

        @JsonProperty("calculated_tax_amount")
        private Long calculatedTaxAmount; // 산출세액

        @JsonProperty("tax_reduction_total_amount")
        private Long taxReductionTotal; // 세액감면

        @JsonProperty("earned_income_total_amount")
        private Long earnedIncomeTotal; // 근로소득세액공제

        @JsonProperty("eligible_children_count")
        private Integer eligibleChildrenCount; // 자녀세액공제 인원

        @JsonProperty("childbirth_adoption_count")
        private Integer childbirthAdoptionCount; // 출산입양

        @JsonProperty("monthly_rent_tax_credit_amount")
        private Long monthlyRentTaxCredit; // 월세세액공제

        @JsonProperty("total_tax_credit_amount")
        private Long totalTaxCredit; // 세액공제 계

        @JsonProperty("determined_tax_amount")
        private Long determinedTaxAmount; // 결정세액 (기납부세액)
    }
}