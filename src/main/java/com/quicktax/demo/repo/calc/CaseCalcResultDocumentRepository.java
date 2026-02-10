package com.quicktax.demo.repo.calc;

import com.quicktax.demo.domain.calc.CaseCalcResultDocument;
import com.quicktax.demo.domain.calc.CaseCalcResultDocumentId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CaseCalcResultDocumentRepository extends JpaRepository<CaseCalcResultDocument, CaseCalcResultDocumentId> {

    List<CaseCalcResultDocument> findAllByIdCaseIdOrderByIdCaseYearAsc(Long caseId);

    List<CaseCalcResultDocument> findAllByIdCaseIdInOrderByIdCaseIdAscIdCaseYearAsc(List<Long> caseIds);
}
