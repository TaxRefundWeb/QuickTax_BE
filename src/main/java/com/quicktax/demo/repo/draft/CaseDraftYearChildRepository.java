package com.quicktax.demo.repo.draft;

import com.quicktax.demo.domain.cases.draft.CaseDraftYearChild;
import com.quicktax.demo.domain.cases.draft.CaseDraftYearChildId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CaseDraftYearChildRepository extends JpaRepository<CaseDraftYearChild, CaseDraftYearChildId> {

    // 💡 [추가] 자녀 목록 조회
    // CaseDraftYearChild -> CaseDraftYear -> Id -> caseId/caseYear 경로로 조회
    @Query("SELECT c FROM CaseDraftYearChild c " +
            "WHERE c.caseDraftYear.id.caseId = :caseId " +
            "AND c.caseDraftYear.id.caseYear = :caseYear")
    List<CaseDraftYearChild> findAllByIdCaseIdAndIdCaseYear(
            @Param("caseId") Long caseId,
            @Param("caseYear") Integer caseYear
    );
}