package com.quicktax.demo.dto.refundInput;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FamilyInfo {

    @JsonProperty("name")
    private String name;

    @JsonProperty("resident_number") // 주민등록번호
    private String residentNumber;
}