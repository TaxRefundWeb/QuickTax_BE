package com.quicktax.demo.dto.refundResult;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefundResultResponse {

    // JSON의 root 키: "refund_results"
    @JsonProperty("refund_results")
    private List<YearlyResult> refundResults;

    // --- 연도별 그룹 ---
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class YearlyResult {
        @JsonProperty("case_year")
        private Integer caseYear;

        @JsonProperty("scenarios")
        private List<ScenarioResult> scenarios;
    }

    // --- 개별 시나리오 (필드 평탄화) ---
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ScenarioResult {

        @JsonProperty("scenario_code")
        private String scenarioCode; // 예: "청년 경정청구 신청"

        @JsonProperty("tax_difference_amount")
        private Long taxDifferenceAmount; // 세액의 차 (파란색)

        @JsonProperty("determined_tax_amount")
        private Long determinedTaxAmount; // 결정세액

        @JsonProperty("tax_base_amount")
        private Long taxBaseAmount; // 종합소득과세표준

        @JsonProperty("calculated_tax")
        private Long calculatedTax; // 산출세액

        @JsonProperty("earned_income_amount")
        private Long earnedIncomeAmount; // 근로소득 (기존 earned_income_tax -> earned_income_amount 변경 반영)

        @JsonProperty("refund_amount")
        private Long refundAmount; // 환급금

        @JsonProperty("scenario_text")
        private String scenarioText; // 세부설명 (줄바꿈 포함 텍스트)
    }
}