package com.quicktax.demo.domain.cases.draft;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "case_draft_year_spouse")
@Getter
@NoArgsConstructor
public class CaseDraftYearSpouse {

    @EmbeddedId
    private CaseDraftYearId id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "case_id", referencedColumnName = "case_id", nullable = false),
            @JoinColumn(name = "case_year", referencedColumnName = "case_year", nullable = false)
    })
    private CaseDraftYear caseDraftYear;

    @Column(name = "spouse_name", length = 30)
    private String spouseName;

    @Column(name = "spouse_rrn", length = 30)
    private String spouseRrn;

    public CaseDraftYearSpouse(
            CaseDraftYear caseDraftYear,
            String spouseName,
            String spouseRrn
    ) {
        this.caseDraftYear = caseDraftYear;
        this.id = caseDraftYear.getId();
        this.spouseName = spouseName;
        this.spouseRrn = spouseRrn;
    }
}
