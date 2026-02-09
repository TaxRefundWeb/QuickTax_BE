package com.quicktax.demo.repo.draft;

import com.quicktax.demo.domain.cases.draft.CaseDraftYearChild;
import com.quicktax.demo.domain.cases.draft.CaseDraftYearChildId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CaseDraftYearChildRepository extends JpaRepository<CaseDraftYearChild, CaseDraftYearChildId> {
}
