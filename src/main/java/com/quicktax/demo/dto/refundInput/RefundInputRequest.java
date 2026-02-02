package com.quicktax.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.quicktax.demo.dto.refundInput.RefundDetailInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class RefundInputRequest {

    @JsonProperty("customerid")
    private Long customerId;

    @JsonProperty("case_year")
    private List<Integer> caseYear;

    @JsonProperty("customers")
    private List<RefundDetailInfo> customers;
}