package com.quicktax.demo.domain.cases.draft;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Embeddable
@Getter
@NoArgsConstructor
public class CaseDraftYearId implements Serializable {

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Min(2000)
    @Max(2100)
    @Column(name = "case_year", nullable = false)
    private Integer caseYear;

    public CaseDraftYearId(Long caseId, Integer caseYear) {
        this.caseId = caseId;
        this.caseYear = caseYear;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CaseDraftYearId that)) return false;
        return Objects.equals(caseId, that.caseId) &&
                Objects.equals(caseYear, that.caseYear);
    }

    @Override
    public int hashCode() {
        return Objects.hash(caseId, caseYear);
    }
}
