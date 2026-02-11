package com.quicktax.demo.repo.ocr;

import com.quicktax.demo.domain.ocr.OcrResultId;
import com.quicktax.demo.domain.ocr.OcrResultPerCompany;
import com.quicktax.demo.domain.ocr.OcrResultPerCompanyId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OcrResultPerCompanyRepository extends JpaRepository<OcrResultPerCompany, OcrResultPerCompanyId> {

    @Query("""
        select p from OcrResultPerCompany p
        where p.id.ocrResultId.caseId = :caseId
        order by p.id.ocrResultId.caseYear asc, p.id.companyId asc
    """)
    List<OcrResultPerCompany> findByCaseIdOrderByYearCompany(@Param("caseId") Long caseId);

    // 재처리 시 연도 단위로 정리할 때 사용
    void deleteByIdOcrResultId(OcrResultId ocrResultId);

    @Query("""
        select p from OcrResultPerCompany p
        where p.id.ocrResultId.caseId = :caseId
          and p.id.ocrResultId.caseYear = :caseYear
        order by p.id.companyId asc
    """)
    List<OcrResultPerCompany> findByCaseIdAndCaseYear(@Param("caseId") Long caseId, @Param("caseYear") Integer caseYear);
}
