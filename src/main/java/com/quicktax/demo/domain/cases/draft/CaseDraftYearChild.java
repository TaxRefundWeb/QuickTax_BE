package com.quicktax.demo.domain.cases.draft;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "case_draft_year_child",
        indexes = {
                @Index(name = "idx_case_draft_year_child_case_year", columnList = "case_year")
        }
)
@Getter
@NoArgsConstructor
public class CaseDraftYearChild {

    @EmbeddedId
    private CaseDraftYearChildId id;

    @MapsId("draftYearId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "case_id", referencedColumnName = "case_id", nullable = false),
            @JoinColumn(name = "case_year", referencedColumnName = "case_year", nullable = false)
    })
    private CaseDraftYear caseDraftYear;

    @Column(name = "child_rrn", length = 30)
    private String childRrn;

    @Column(name = "child_name", length = 30)
    private String childName;

    public CaseDraftYearChild(CaseDraftYear caseDraftYear, Integer childId, String childRrn, String childName) {
        this.caseDraftYear = caseDraftYear;
        this.id = new CaseDraftYearChildId(caseDraftYear.getId(), childId);
        this.childRrn = childRrn;
        this.childName = childName;
    }
}
