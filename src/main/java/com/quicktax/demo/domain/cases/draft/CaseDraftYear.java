package com.quicktax.demo.domain.cases.draft;

import com.quicktax.demo.domain.cases.TaxCase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "case_draft_year")
@Getter
@NoArgsConstructor
public class CaseDraftYear {

    @EmbeddedId
    private CaseDraftYearId id;

    @MapsId("caseId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "case_id", nullable = false)
    private TaxCase taxCase;

    @Column(name = "small_business_yn", nullable = false)
    private boolean smallBusinessYn = false;

    @Column(name = "spouse_yn", nullable = false)
    private boolean spouseYn = false;

    @Column(name = "child_yn", nullable = false)
    private boolean childYn = false;

    @Column(name = "reduction_yn", nullable = false)
    private boolean reductionYn = false;

    public CaseDraftYear(TaxCase taxCase, Integer caseYear) {
        this.taxCase = taxCase;
        this.id = new CaseDraftYearId(taxCase.getCaseId(), caseYear);
    }

    public CaseDraftYear(
            TaxCase taxCase,
            Integer caseYear,
            boolean smallBusinessYn,
            boolean spouseYn,
            boolean childYn
    ) {
        this.taxCase = taxCase;
        this.id = new CaseDraftYearId(taxCase.getCaseId(), caseYear);
        this.smallBusinessYn = smallBusinessYn;
        this.spouseYn = spouseYn;
        this.childYn = childYn;
    }

    public CaseDraftYear(
            TaxCase taxCase,
            Integer caseYear,
            boolean smallBusinessYn,
            boolean spouseYn,
            boolean childYn,
            boolean reductionYn
    ) {
        this.taxCase = taxCase;
        this.id = new CaseDraftYearId(taxCase.getCaseId(), caseYear);
        this.smallBusinessYn = smallBusinessYn;
        this.spouseYn = spouseYn;
        this.childYn = childYn;
        this.reductionYn = reductionYn;
    }
}
