package com.quicktax.demo.dto.calc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Getter
@NoArgsConstructor
@ToString
public class CalcConfirmRequest {

    // JSON Body: { "scenarios": [ ... ] }
    @JsonProperty("scenarios")
    private List<YearScenario> scenarios;

    @Getter
    @NoArgsConstructor
    @ToString
    public static class YearScenario {

        @JsonProperty("case_year")
        private Integer caseYear;

        // 예: "청년 경정청구 신청", "이중근로 경정청구 신청" 등
        @JsonProperty("scenario_code")
        private String scenarioCode;
    }
}