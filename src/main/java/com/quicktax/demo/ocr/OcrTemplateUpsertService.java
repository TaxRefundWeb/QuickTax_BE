package com.quicktax.demo.ocr;

import com.quicktax.demo.common.ApiException;
import com.quicktax.demo.common.ErrorCode;
import com.quicktax.demo.domain.cases.TaxCase;
import com.quicktax.demo.domain.ocr.OcrResult;
import com.quicktax.demo.domain.ocr.OcrResultId;
import com.quicktax.demo.domain.ocr.OcrResultPerCompany;
import com.quicktax.demo.domain.ocr.OcrResultPerCompanyId;
import com.quicktax.demo.repo.TaxCaseRepository;
import com.quicktax.demo.repo.ocr.OcrResultPerCompanyRepository;
import com.quicktax.demo.repo.ocr.OcrResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class OcrTemplateUpsertService {

    private final TaxCaseRepository taxCaseRepository;
    private final OcrResultRepository ocrResultRepository;
    private final OcrResultPerCompanyRepository ocrResultPerCompanyRepository;

    /** 재업로드/재처리 시 이전 데이터가 남지 않게 연도 단위로 리셋 */
    @Transactional
    public void resetYear(Long caseId, int caseYear) {
        OcrResultId id = new OcrResultId(caseId, caseYear);

        // 자식 먼저
        ocrResultPerCompanyRepository.deleteByIdOcrResultId(id);

        // 부모
        if (ocrResultRepository.existsById(id)) {
            ocrResultRepository.deleteById(id);
        }
    }

    /**
     * salary 템플릿(= page1/page4)
     * - salary1~3만 사용
     * - 값 없는 salary는 row 생성 안 함
     * - companyOffset:
     *   - page1이면 0 => companyId 1..3
     *   - page4면 3 => companyId 4..6
     */
    @Transactional
    public void upsertSalaryPage(Long caseId, int caseYear, Map<String, String> fields, int companyOffset) {
        OcrResult ocrResult = getOrCreateOcrResult(caseId, caseYear);

        for (int i = 1; i <= 3; i++) {
            Long salary = OcrNumberSanitizer.toLongOrNull(fields.get("salary" + i));
            if (salary == null) continue;

            Integer companyId = companyOffset + i;
            OcrResultPerCompanyId pid = new OcrResultPerCompanyId(ocrResult.getId(), companyId);

            OcrResultPerCompany row = ocrResultPerCompanyRepository.findById(pid)
                    .orElseGet(() -> new OcrResultPerCompany(ocrResult, companyId, salary));

            row.updateSalary(salary);
            ocrResultPerCompanyRepository.save(row);
        }
    }

    /**
     * detail 템플릿(= page2/page5)
     * - add=false: 그냥 set (3장짜리, 또는 6장짜리의 2p)
     * - add=true : 기존값과 합산 (6장짜리의 5p)
     */
    @Transactional
    public void upsertDetailPage(Long caseId, int caseYear, Map<String, String> fields, boolean add) {
        OcrResult ocrResult = getOrCreateOcrResult(caseId, caseYear);

        if (add) ocrResult.addTemplateNumbers(fields);
        else ocrResult.applyTemplateNumbers(fields);

        ocrResultRepository.save(ocrResult);
    }

    private OcrResult getOrCreateOcrResult(Long caseId, int caseYear) {
        OcrResultId id = new OcrResultId(caseId, caseYear);

        return ocrResultRepository.findById(id)
                .orElseGet(() -> {
                    TaxCase taxCase = taxCaseRepository.findById(caseId)
                            .orElseThrow(() -> new ApiException(ErrorCode.COMMON404, "존재하지 않는 caseId 입니다."));
                    return ocrResultRepository.save(new OcrResult(taxCase, caseYear));
                });
    }
}
