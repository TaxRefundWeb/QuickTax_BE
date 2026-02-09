package com.quicktax.demo.domain.calc;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CaseCalcResultId implements Serializable {

    @Column(name = "case_id")
    private Long caseId;

    @Column(name = "case_year")
    private Integer caseYear;

    @Column(name = "scenario_code", length = 40)
    private String scenarioCode;
}
