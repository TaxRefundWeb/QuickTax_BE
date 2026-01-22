package com.quicktax.demo.domain.calc;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor
@EqualsAndHashCode
public class CaseCalcResultId implements Serializable {

    @Column(name = "case_id")
    @NotNull
    private Long caseId;

    @Column(name = "case_year")
    @NotNull
    @Min(2000)
    @Max(2100)
    private Integer caseYear;

    @Column(name = "scenario_code", length = 40)
    @NotNull
    @Size(max = 40)
    private String scenarioCode;

    public CaseCalcResultId(Long caseId, Integer caseYear, String scenarioCode) {
        this.caseId = caseId;
        this.caseYear = caseYear;
        this.scenarioCode = scenarioCode;
    }
}
