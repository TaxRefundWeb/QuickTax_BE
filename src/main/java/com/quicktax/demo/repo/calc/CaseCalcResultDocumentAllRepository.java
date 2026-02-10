package com.quicktax.demo.repo.calc;

import com.quicktax.demo.domain.calc.CaseCalcResultDocumentAll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CaseCalcResultDocumentAllRepository extends JpaRepository<CaseCalcResultDocumentAll, Long> {
}