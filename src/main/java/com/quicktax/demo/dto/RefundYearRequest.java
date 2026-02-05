package com.quicktax.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@ToString
public class RefundYearRequest {

    // 1. 경정청구 기간 (시작일 ~ 종료일) -> 이를 통해 연도를 계산함
    @JsonProperty("claim_from")
    private String claimFrom; // 예: "2019-01-01"

    @JsonProperty("claim_to")
    private String claimTo;   // 예: "2024-12-31"

    // 2. 경정청구 신청일 (신규 추가, 기존 claim_year 대체)
    @JsonProperty("claim_date")
    private String claimDate; // 예: "2026-02-05"

    // 3. 감면 정보
    @JsonProperty("reduction_yn")
    private String reductionYn;

    @JsonProperty("reduction_start") // null 허용
    private String reductionStart;

    @JsonProperty("reduction_end")   // null 허용
    private String reductionEnd;
}