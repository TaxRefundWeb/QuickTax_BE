package com.quicktax.demo.repo.draft;

import com.quicktax.demo.domain.cases.draft.CaseDraftYear;
import com.quicktax.demo.domain.cases.draft.CaseDraftYearId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CaseDraftYearRepository extends JpaRepository<CaseDraftYear, CaseDraftYearId> {
    List<CaseDraftYear> findAllByIdCaseIdOrderByIdCaseYearAsc(Long caseId);
}

