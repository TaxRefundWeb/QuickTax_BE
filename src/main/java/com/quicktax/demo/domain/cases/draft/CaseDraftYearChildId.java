package com.quicktax.demo.domain.cases.draft;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor
@EqualsAndHashCode
public class CaseDraftYearChildId implements Serializable {

    @Embedded
    private CaseDraftYearId draftYearId;

    @Column(name = "child_id")
    private Integer childId;

    public CaseDraftYearChildId(CaseDraftYearId draftYearId, Integer childId) {
        this.draftYearId = draftYearId;
        this.childId = childId;
    }
}
