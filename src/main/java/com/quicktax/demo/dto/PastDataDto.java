package com.quicktax.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * 1. 개별 과거 기록 정보
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PastDataDto {
    @JsonProperty("case_id")
    private Long caseId;

    @JsonProperty("case_date")
    private String caseDate; // "2026-01-28" 형식

    @JsonProperty("scenario_code")
    private String scenarioCode;

    @JsonProperty("determined_tax_amount")
    private Long determinedTaxAmount;

    @JsonProperty("refund_amount")
    private Long refundAmount;
}

