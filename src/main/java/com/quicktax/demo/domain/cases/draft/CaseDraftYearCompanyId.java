package com.quicktax.demo.domain.cases.draft;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
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
public class CaseDraftYearCompanyId implements Serializable {

    @Embedded
    @NotNull
    private CaseDraftYearId draftYearId;

    @NotNull
    @Min(1)
    @Max(3)
    @Column(name = "case_company", nullable = false)
    private Integer caseCompany;

    public CaseDraftYearCompanyId(CaseDraftYearId draftYearId, Integer caseCompany) {
        this.draftYearId = draftYearId;
        this.caseCompany = caseCompany;
    }
}
