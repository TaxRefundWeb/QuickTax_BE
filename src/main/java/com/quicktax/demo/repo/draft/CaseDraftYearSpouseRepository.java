package com.quicktax.demo.repo.draft;

import com.quicktax.demo.domain.cases.draft.CaseDraftYearId;
import com.quicktax.demo.domain.cases.draft.CaseDraftYearSpouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CaseDraftYearSpouseRepository extends JpaRepository<CaseDraftYearSpouse, CaseDraftYearId> {

    // 💡 [추가] 배우자 단건 조회
    // CaseDraftYearSpouse의 ID 자체가 CaseDraftYearId이므로 바로 조회 가능
    @Query("SELECT s FROM CaseDraftYearSpouse s " +
            "WHERE s.id.caseId = :caseId " +
            "AND s.id.caseYear = :caseYear")
    Optional<CaseDraftYearSpouse> findByIdCaseIdAndIdCaseYear(
            @Param("caseId") Long caseId,
            @Param("caseYear") Integer caseYear
    );
}