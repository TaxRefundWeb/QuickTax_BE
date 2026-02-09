package com.quicktax.demo.repo.draft;

import com.quicktax.demo.domain.cases.draft.CaseDraftYearCompany;
import com.quicktax.demo.domain.cases.draft.CaseDraftYearCompanyId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CaseDraftYearCompanyRepository extends JpaRepository<CaseDraftYearCompany, CaseDraftYearCompanyId> {
}
