package com.quicktax.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PastDataDto {

    @JsonProperty("case_id")
    private Long caseId;

    @JsonProperty("case_year")
    private Integer caseYear;

    @JsonProperty("claim_date")
    private LocalDate claimDate; // "YYYY-MM-DD"로 JSON 나감

    @JsonProperty("scenario_code")
    private String scenarioCode;

    @JsonProperty("determined_tax_amount")
    private Long determinedTaxAmount;

    @JsonProperty("refund_amount")
    private Long refundAmount;

    @JsonProperty("url")
    private String url;
}
