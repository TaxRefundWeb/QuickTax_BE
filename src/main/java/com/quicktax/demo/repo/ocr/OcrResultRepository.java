package com.quicktax.demo.repo.ocr;

import com.quicktax.demo.domain.ocr.OcrResult;
import com.quicktax.demo.domain.ocr.OcrResultId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OcrResultRepository extends JpaRepository<OcrResult, OcrResultId> {

    // 1. 기본 제공 findById(OcrResultId id) -> OcrService의 저장 로직에서 사용됨 (이미 존재)

    // 2. 특정 Case의 모든 연도 데이터 조회 (연도 오름차순) -> 조회 API에서 사용 가능
    // Entity의 필드명이 'id'이고, 그 안의 필드명이 'caseId', 'caseYear'일 때 정확히 동작합니다.
    List<OcrResult> findByIdCaseIdOrderByIdCaseYearAsc(Long caseId);
}