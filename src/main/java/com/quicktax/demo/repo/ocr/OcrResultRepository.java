package com.quicktax.demo.repo.ocr;

import com.quicktax.demo.domain.ocr.OcrResult;
import com.quicktax.demo.domain.ocr.OcrResultId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional; // 💡 Optional Import 필수!

@Repository
public interface OcrResultRepository extends JpaRepository<OcrResult, OcrResultId> {

    // 1. 기존 메서드 유지
    List<OcrResult> findByIdCaseIdOrderByIdCaseYearAsc(Long caseId);

    // 💡 [추가] CalcService에서 호출하는 메서드 정의
    // 복합키(OcrResultId) 내부의 caseId와 caseYear를 조건으로 단건 조회
    Optional<OcrResult> findByIdCaseIdAndIdCaseYear(Long caseId, Integer caseYear);
}