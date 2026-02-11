package com.quicktax.demo.repo.draft;

import com.quicktax.demo.domain.cases.draft.CaseDraftYear;
import com.quicktax.demo.domain.cases.draft.CaseDraftYearId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional; // 💡 Optional Import 필수!

public interface CaseDraftYearRepository extends JpaRepository<CaseDraftYear, CaseDraftYearId> {

    // 1. 기존 메서드 유지
    List<CaseDraftYear> findAllByIdCaseIdOrderByIdCaseYearAsc(Long caseId);

    // 💡 [추가] CalcService에서 호출하는 메서드 정의
    // 복합키(CaseDraftYearId) 내부의 caseId와 caseYear를 조건으로 단건 조회
    Optional<CaseDraftYear> findByIdCaseIdAndIdCaseYear(Long caseId, Integer caseYear);
}