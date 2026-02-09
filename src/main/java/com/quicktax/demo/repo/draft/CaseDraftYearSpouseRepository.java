package com.quicktax.demo.repo.draft;

import com.quicktax.demo.domain.cases.draft.CaseDraftYearId;
import com.quicktax.demo.domain.cases.draft.CaseDraftYearSpouse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CaseDraftYearSpouseRepository extends JpaRepository<CaseDraftYearSpouse, CaseDraftYearId> {
}
