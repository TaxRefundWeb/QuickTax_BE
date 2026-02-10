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

    // 프론트엔드가 { "OCRData": [ ... ] } 형태로 보낸다고 가정
    @JsonProperty("OCRData")
    private List<OcrYearData> ocrData;

    @Getter
    @NoArgsConstructor
    @ToString
    public static class OcrYearData {

        @JsonProperty("case_year")
        private Integer caseYear;

        @JsonProperty("total_salary")
        private Long totalSalary;

        @JsonProperty("earned_income_deduction_amount")
        private Long earnedIncomeDeduction;

        @JsonProperty("earned_income_amount")
        private Long earnedIncomeAmount;

        @JsonProperty("basic_deduction_self_amount")
        private Long basicDeductionSelf;

        @JsonProperty("basic_deduction_spouse_amount")
        private Long basicDeductionSpouse;

        @JsonProperty("basic_deduction_dependents_amount")
        private Long basicDeductionDependents;

        @JsonProperty("national_pension_deduction_amount")
        private Long nationalPensionDeduction;

        // 🚨 [수정 제안] 다른 필드들과 규칙 통일 (프론트와 확인 필수!)
        // 기존: @JsonProperty("TotalSpecialIncomeDeduction")
        @JsonProperty("total_special_income_deduction_amount")
        private Long totalSpecialIncomeDeduction;

        @JsonProperty("adjusted_income_amount")
        private Long adjustedIncomeAmount;

        @JsonProperty("other_income_deduction_total_amount")
        private Long otherIncomeDeductionTotal;

        // 💡 [추가] 엔티티에는 있는데 DTO에 없던 필드 (그밖의 소득공제 추가분)
        @JsonProperty("other_income_deduction_extra")
        private Long otherIncomeDeductionExtra;

        @JsonProperty("tax_base_amount")
        private Long taxBaseAmount;

        @JsonProperty("calculated_tax_amount")
        private Long calculatedTaxAmount;

        @JsonProperty("tax_reduction_total_amount")
        private Long taxReductionTotal;

        @JsonProperty("earned_income_total_amount")
        private Long earnedIncomeTotal;

        @JsonProperty("eligible_children_count")
        private Integer eligibleChildrenCount;

        @JsonProperty("childbirth_adoption_count")
        private Integer childbirthAdoptionCount;

        // 💡 [추가] 엔티티에는 있는데 DTO에 없던 필드 (기부금)
        @JsonProperty("donation_total_amount")
        private Long donationTotalAmount;

        // 💡 [추가] 엔티티에는 있는데 DTO에 없던 필드 (표준세액공제)
        @JsonProperty("standard_tax_credit")
        private Long standardTaxCredit;

        @JsonProperty("monthly_rent_tax_credit_amount")
        private Long monthlyRentTaxCredit;

        @JsonProperty("total_tax_credit_amount")
        private Long totalTaxCredit;

        // 💡 [매핑 주의] 엔티티: determinedTaxAmountOrigin / JSON: determined_tax_amount
        @JsonProperty("determined_tax_amount")
        private Long determinedTaxAmount;
    }
}