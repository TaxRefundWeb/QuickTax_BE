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
import com.quicktax.demo.service.calc.CalcService; // 💡 계산 엔진 서비스 임포트
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
    private final CalcService calcService; // 💡 새로 만든 계산 엔진 주입

    /**
     * OCR 확정 및 내부 계산 실행
     */
    @Transactional
    public void confirmOcrDataAndCalculate(Long cpaId, Long caseId, OcrConfirmRequest request) {

        // 1. Case 및 권한 검증
        TaxCase taxCase = taxCaseRepository.findById(caseId)
                .orElseThrow(() -> new ApiException(ErrorCode.COMMON404, "존재하지 않는 Case ID입니다."));

        Long ownerCpaId = taxCase.getCustomer().getTaxCompany().getCpaId();
        if (!cpaId.equals(ownerCpaId)) {
            throw new ApiException(ErrorCode.AUTH403, "권한이 존재하지 않습니다.");
        }

        // 2. OCR 작업 상태 확인
        OcrJob ocrJob = ocrJobRepository.findById(caseId)
                .orElseThrow(() -> new ApiException(ErrorCode.COMMON404, "OCR 요청 내역이 존재하지 않습니다."));

        if (ocrJob.getStatus() == OcrJobStatus.FAILED) {
            throw new ApiException(ErrorCode.COMMON500, "OCR 분석에 실패했습니다.");
        }
        if (ocrJob.getStatus() != OcrJobStatus.READY) {
            throw new ApiException(ErrorCode.OCR409, "OCR 분석이 아직 완료되지 않았습니다.");
        }

        // 3. 연도별 데이터 저장 및 계산 실행
        for (OcrYearData data : request.getOcrData()) {
            Integer year = data.getCaseYear();
            log.info("OCR 확정 데이터 저장 및 계산 시작: CaseId={}, Year={}", caseId, year);

            // 3-1. OCR 결과 저장/업데이트
            OcrResultId resultId = new OcrResultId(caseId, year);
            OcrResult ocrResult = ocrResultRepository.findById(resultId)
                    .orElseGet(() -> new OcrResult(taxCase, year));

            ocrResult.updateData(data);
            ocrResultRepository.save(ocrResult);

            // 3-2. 🚀 [핵심 연결] 데이터가 저장된 직후 바로 계산 엔진 가동
            // 각 연도별로 루프 안에서 호출하여, 최신화된 OCR 데이터를 기반으로 계산을 수행합니다.
            calcService.runCalculation(caseId, year);
        }

        log.info("Case ID: {} 모든 연도에 대한 OCR 확정 및 내부 계산 완료.", caseId);
    }
}