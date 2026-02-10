package com.quicktax.demo.dto.refund;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class RefundResultsResponse {

    @JsonProperty("refund_results")
    private List<YearResult> refundResults;

    @Getter
    @AllArgsConstructor
    public static class YearResult {
        @JsonProperty("case_year")
        private Integer caseYear;

        private List<ScenarioResult> scenarios;
    }

    @Getter
    @AllArgsConstructor
    public static class ScenarioResult {
        @JsonProperty("scenario_code")
        private String scenarioCode;

        @JsonProperty("tax_difference_amount")
        private Long taxDifferenceAmount;

        @JsonProperty("determined_tax_amount")
        private Long determinedTaxAmount;

        @JsonProperty("tax_base_amount")
        private Long taxBaseAmount;

        @JsonProperty("calculated_tax")
        private Long calculatedTax;

        @JsonProperty("earned_income_amount")
        private Long earnedIncomeAmount;

        @JsonProperty("refund_amount")
        private Long refundAmount;

        @JsonProperty("scenario_text")
        private String scenarioText;
    }
}
