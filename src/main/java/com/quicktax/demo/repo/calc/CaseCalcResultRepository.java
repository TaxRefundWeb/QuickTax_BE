package com.quicktax.demo.repo.calc;

import com.quicktax.demo.domain.calc.CaseCalcResult;
import com.quicktax.demo.domain.calc.CaseCalcResultId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CaseCalcResultRepository extends JpaRepository<CaseCalcResult, CaseCalcResultId> {
    List<CaseCalcResult> findAllByIdCaseIdOrderByIdCaseYearAscIdScenarioCodeAsc(Long caseId);
}
