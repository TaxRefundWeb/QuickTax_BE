package com.quicktax.demo.service.ocr;

import com.quicktax.demo.common.ApiException;
import com.quicktax.demo.common.ErrorCode;
import com.quicktax.demo.domain.cases.TaxCase;
import com.quicktax.demo.domain.ocr.OcrJob;
import com.quicktax.demo.domain.ocr.OcrJobStatus;
import com.quicktax.demo.domain.ocr.OcrResult;
import com.quicktax.demo.domain.ocr.OcrResultId;
import com.quicktax.demo.dto.OcrConfirmRequest;
import com.quicktax.demo.dto.OcrConfirmRequest.OcrYearData;
import com.quicktax.demo.repo.TaxCaseRepository;
import com.quicktax.demo.repo.ocr.OcrJobRepository;
import com.quicktax.demo.repo.ocr.OcrResultRepository;
import com.quicktax.demo.service.calc.CalcService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OcrService {

    private final TaxCaseRepository taxCaseRepository;
    private final OcrJobRepository ocrJobRepository;
    private final OcrResultRepository ocrResultRepository;
    private final CalcService calcService;

    private void requireLogin(Long cpaId) {
        if (cpaId == null) throw new ApiException(ErrorCode.AUTH401, "로그인이 필요합니다.");
    }

    private TaxCase requireOwnedCase(Long cpaId, Long caseId) {
        TaxCase taxCase = taxCaseRepository.findById(caseId)
                .orElseThrow(() -> new ApiException(ErrorCode.COMMON404, "존재하지 않는 Case ID입니다."));

        Long ownerCpaId = taxCase.getCustomer().getTaxCompany().getCpaId();
        if (!cpaId.equals(ownerCpaId)) throw new ApiException(ErrorCode.AUTH403, "권한이 존재하지 않습니다.");
        return taxCase;
    }

    /**
     * OCR 확정 및 내부 계산 실행
     */
    @Transactional
    public void confirmOcrDataAndCalculate(Long cpaId, Long caseId, OcrConfirmRequest request) {
        requireLogin(cpaId);

        // 1) 권한 검증
        TaxCase taxCase = requireOwnedCase(cpaId, caseId);

        // 2) OCR 작업 상태 확인
        OcrJob ocrJob = ocrJobRepository.findById(caseId)
                .orElseThrow(() -> new ApiException(ErrorCode.COMMON404, "OCR 요청 내역이 존재하지 않습니다."));

        if (ocrJob.getStatus() == OcrJobStatus.FAILED) {
            throw new ApiException(ErrorCode.COMMON500, "OCR 분석에 실패했습니다.");
        }
        if (ocrJob.getStatus() != OcrJobStatus.READY) {
            throw new ApiException(ErrorCode.OCR429, "OCR 분석이 아직 완료되지 않았습니다.");
        }

        if (request == null || request.getOcrData() == null) {
            throw new ApiException(ErrorCode.BADREQ400, "확정할 OCR 데이터가 없습니다.");
        }

        // 3) 연도별 데이터 저장 + 계산 실행
        for (OcrYearData data : request.getOcrData()) {
            Integer year = data.getCaseYear();
            if (year == null) {
                throw new ApiException(ErrorCode.BADREQ400, "caseYear는 필수입니다.");
            }

            log.info("OCR 확정 데이터 저장 및 계산 시작: caseId={}, year={}", caseId, year);

            OcrResultId resultId = new OcrResultId(caseId, year);
            OcrResult ocrResult = ocrResultRepository.findById(resultId)
                    .orElseGet(() -> new OcrResult(taxCase, year));

            // ⚠️ OcrResult에 updateData(OcrYearData)가 존재한다는 전제(네 코드 기준)
            ocrResult.updateData(data);
            ocrResultRepository.save(ocrResult);

            // 최신 OCR 기반으로 즉시 계산 트리거
            calcService.runCalculation(caseId, year);
        }

        log.info("caseId={} 모든 연도 OCR 확정 및 내부 계산 완료", caseId);
    }
}
