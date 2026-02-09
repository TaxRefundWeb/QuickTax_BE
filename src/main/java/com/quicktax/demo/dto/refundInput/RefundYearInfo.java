package com.quicktax.demo.dto.refundInput;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class RefundYearInfo {

    @JsonProperty("target_year") // 해당 정보가 어떤 연도 것인지 (예: 2024)
    private Integer targetYear;

    @JsonProperty("company_name") // 법인명
    private String companyName;

    @JsonProperty("work_start_date") // 근무 시작일 (YYYY-MM-DD)
    private String workStartDate;

    @JsonProperty("work_end_date") // 근무 종료일
    private String workEndDate;

    @JsonProperty("is_sme") // 중소기업 여부 (true/false)
    private Boolean isSme;

    @JsonProperty("business_reg_number") // 사업자 등록 번호
    private String businessRegNumber;

    @JsonProperty("reduction_start_date") // 감면 시작일
    private String reductionStartDate;

    @JsonProperty("reduction_end_date") // 감면 종료일
    private String reductionEndDate;

    @JsonProperty("submission_date") // 서류 제출 일자
    private String submissionDate;

    // --- 가족 관련 ---

    @JsonProperty("has_spouse") // 배우자 유무
    private Boolean hasSpouse;

    @JsonProperty("spouse_info") // 배우자 정보 (유무가 true일 때만 값 존재)
    private FamilyInfo spouseInfo;

    @JsonProperty("has_child") // 자녀 유무
    private Boolean hasChild;

    @JsonProperty("children_info") // 자녀 리스트 (자녀 수만큼 동적 배열)
    private List<FamilyInfo> childrenInfo;
}