package com.quicktax.demo.repo.ocr;

import com.quicktax.demo.domain.ocr.OcrResult;
import com.quicktax.demo.domain.ocr.OcrResultId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OcrResultRepository extends JpaRepository<OcrResult, OcrResultId> {
    List<OcrResult> findByIdCaseIdOrderByIdCaseYearAsc(Long caseId);
}