package com.quicktax.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChildInfo {

    @JsonProperty("child_yn") // 자녀 유무 (각 자녀 객체에 포함)
    private String childYn;

    @JsonProperty("child_name")
    private String childName;

    @JsonProperty("child_RRN")
    private String childRrn;
}