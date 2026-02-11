package com.quicktax.demo.repo.draft;

import com.quicktax.demo.domain.cases.draft.CaseDraftYearCompany;
import com.quicktax.demo.domain.cases.draft.CaseDraftYearCompanyId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CaseDraftYearCompanyRepository extends JpaRepository<CaseDraftYearCompany, CaseDraftYearCompanyId> {

    // 💡 [추가] CalcService에서 호출하는 메서드 구현
    // 엔티티의 caseDraftYear 관계를 타고 들어가서 caseId와 caseYear를 비교합니다.
    @Query("SELECT c FROM CaseDraftYearCompany c " +
            "WHERE c.caseDraftYear.id.caseId = :caseId " +
            "AND c.caseDraftYear.id.caseYear = :caseYear")
    List<CaseDraftYearCompany> findAllByIdCaseIdAndIdCaseYear(
            @Param("caseId") Long caseId,
            @Param("caseYear") Integer caseYear
    );
}