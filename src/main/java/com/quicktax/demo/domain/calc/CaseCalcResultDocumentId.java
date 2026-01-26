package com.quicktax.demo.domain.calc;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor
@EqualsAndHashCode
public class CaseCalcResultDocumentId implements Serializable {

    @Column(name = "case_id")
    @NotNull
    private Long caseId;

    @Column(name = "case_year")
    @NotNull
    @Min(2000)
    @Max(2100)
    private Integer caseYear;

    public CaseCalcResultDocumentId(Long caseId, Integer caseYear) {
        this.caseId = caseId;
        this.caseYear = caseYear;
    }
}
