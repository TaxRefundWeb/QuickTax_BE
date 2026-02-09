package com.quicktax.demo.dto.refundInput;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Getter
@NoArgsConstructor
@ToString
public class RefundInputRequest {

    // Root: "cases" 배열
    @JsonProperty("cases")
    private List<RefundYearlyData> cases;

    @Getter
    @NoArgsConstructor
    @ToString
    public static class RefundYearlyData {

        @JsonProperty("case_year")
        private Integer caseYear;

        @JsonProperty("spouse_yn")
        private Boolean spouseYn;

        @JsonProperty("child_yn")
        private Boolean childYn;

        @JsonProperty("companies")
        private List<CompanyInfo> companies;

        @JsonProperty("spouse")
        private SpouseInfo spouse;

        @JsonProperty("children")
        private List<ChildInfo> children;
    }

    @Getter
    @NoArgsConstructor
    @ToString
    public static class CompanyInfo {

        @JsonProperty("business_number")
        private String businessNumber;

        @JsonProperty("case_work_start")
        private String caseWorkStart;

        @JsonProperty("case_work_end")
        private String caseWorkEnd;

        @JsonProperty("small_business_yn")
        private Boolean smallBusinessYn;
    }

    @Getter
    @NoArgsConstructor
    @ToString
    public static class SpouseInfo {

        @JsonProperty("spouse_name")
        private String spouseName;

        @JsonProperty("spouse_rrn")
        private String spouseRrn;
    }

    @Getter
    @NoArgsConstructor
    @ToString
    public static class ChildInfo {

        @JsonProperty("child_name")
        private String childName;

        @JsonProperty("child_rrn")
        private String childRrn;
    }
}