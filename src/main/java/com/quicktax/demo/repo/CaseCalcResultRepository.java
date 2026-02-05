package com.quicktax.demo.repo;

import com.quicktax.demo.domain.calc.CaseCalcResult;
import com.quicktax.demo.domain.calc.CaseCalcResultId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CaseCalcResultRepository extends JpaRepository<CaseCalcResult, CaseCalcResultId> {

    // 특정 Case ID에 해당하는 모든 결과를 조회
    @Query("SELECT c FROM CaseCalcResult c WHERE c.id.caseId = :caseId ORDER BY c.id.caseYear ASC")
    List<CaseCalcResult> findAllByCaseId(@Param("caseId") Long caseId);
}