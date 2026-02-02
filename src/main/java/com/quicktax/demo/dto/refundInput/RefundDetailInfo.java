package com.quicktax.demo.dto.refundInput;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.quicktax.demo.dto.ChildInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class RefundDetailInfo {

    @JsonProperty("Business_number")
    private String businessNumber;

    @JsonProperty("small_business_yn")
    private String smallBusinessYn; // "yes" or "no"

    @JsonProperty("case_work_start")
    private String caseWorkStart;

    @JsonProperty("case_work_end")
    private String caseWorkEnd;

    @JsonProperty("claim_date")
    private String claimDate;

    @JsonProperty("reduction_start")
    private String reductionStart;

    @JsonProperty("reduction_end")
    private String reductionEnd;

    // --- 💡 배우자 정보 (객체 없이 Flat하게 필드로 선언) ---
    @JsonProperty("spouse_yn")
    private String spouseYn;

    @JsonProperty("spouse_name")
    private String spouseName;

    @JsonProperty("spouse_RRN")
    private String spouseRrn;

    // --- 💡 자녀 정보 (중복 키 불가로 인해 리스트 사용) ---
    @JsonProperty("child_list")
    private List<ChildInfo> childList;
}