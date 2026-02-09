package com.quicktax.demo.dto.refundInput;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class RefundSaveResponse {

    @JsonProperty("saved_case_years")
    private List<Integer> savedCaseYears;
}