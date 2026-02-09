package com.quicktax.demo.dto.refundInput;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WithholdingUploadRequest {

    @JsonProperty("case_id") // 요청하신 키 값 case_id 반영
    private Long caseId;

    @JsonProperty("claim_from") // 시작일 (예: 2019-01-01)
    private String claimFrom;

    @JsonProperty("claim_to")   // 종료일 (예: 2024-12-31)
    private String claimTo;
}