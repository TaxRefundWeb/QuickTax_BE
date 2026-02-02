package com.quicktax.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RefundYearRequest {

    @JsonProperty("claim_from")
    private String claimFrom;  // "2025-01-01" (문자열)

    @JsonProperty("claim_to")
    private String claimTo;    // "2021-12-31" (문자열)
}