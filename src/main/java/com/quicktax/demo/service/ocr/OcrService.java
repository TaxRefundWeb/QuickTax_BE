package com.quicktax.demo.service.ocr;

import com.quicktax.demo.common.ApiException;
import com.quicktax.demo.common.ErrorCode;
import com.quicktax.demo.domain.cases.TaxCase;
import com.quicktax.demo.domain.ocr.OcrJob;
import com.quicktax.demo.domain.ocr.OcrJobStatus;
import com.quicktax.demo.domain.ocr.OcrResult;
import com.quicktax.demo.domain.ocr.OcrResultId; // 💡 복합키 Import
import com.quicktax.demo.dto.OcrConfirmRequest;
import com.quicktax.demo.dto.OcrConfirmRequest.OcrYearData;
import com.quicktax.demo.repo.TaxCaseRepository;
import com.quicktax.demo.repo.ocr.OcrJobRepository;
import com.quicktax.demo.repo.ocr.OcrResultRepository; // 💡 저장소 Import
import com.quicktax.demo.service.result.RefundResultService;
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
    private final OcrResultRepository ocrResultRepository; // 💡 데이터를 저장할 리포지토리
    private final RefundResultService refundCalculationService;

    /**
     * OCR 확정 및 환급액 계산 요청 처리
     * 1. Case 및 권한 검증
     * 2. OCR 완료 상태 검증 (완료되지 않았으면 409, 실패했으면 500 리턴)
     * 3. DB(OcrResult)에 수정된 데이터 저장 (없으면 생성, 있으면 업데이트)
     * 4. 환급액 계산 로직 실행
     */
    @Transactional
    public void confirmOcrDataAndCalculate(Long cpaId, Long caseId, OcrConfirmRequest request) {

        // 1. Case 조회
        TaxCase taxCase = taxCaseRepository.findById(caseId)
                .orElseThrow(() -> new ApiException(ErrorCode.COMMON404, "존재하지 않는 Case ID입니다."));

        // 2. CPA 권한 검증
        Long ownerCpaId = taxCase.getCustomer().getTaxCompany().getCpaId();
        if (!cpaId.equals(ownerCpaId)) {
            throw new ApiException(ErrorCode.AUTH403, "권한이 존재하지 않습니다. 다시 로그인 해주세요.");
        }

        // 3. OCR 작업 상태 확인
        OcrJob ocrJob = ocrJobRepository.findById(caseId)
                .orElseThrow(() -> new ApiException(ErrorCode.COMMON404, "OCR 요청 내역이 존재하지 않습니다."));

        // 상태 검사: 실패(FAILED) -> 500 에러 / 미완료(!READY) -> 409 에러
        if (ocrJob.getStatus() == OcrJobStatus.FAILED) {
            throw new ApiException(ErrorCode.COMMON500, "OCR 분석에 실패했습니다. 이미지를 다시 업로드해주세요.");
        }
        if (ocrJob.getStatus() != OcrJobStatus.READY) {
            throw new ApiException(ErrorCode.OCR409, "OCR 분석이 아직 완료되지 않았습니다. 잠시 후 다시 시도해주세요.");
        }

        // 4. [저장 단계] OcrResult 테이블에 데이터 저장 (핵심 로직)
        for (OcrYearData data : request.getOcrData()) {
            log.info("OCR 확정 데이터 저장: CaseId={}, 연도={}, 총급여={}", caseId, data.getCaseYear(), data.getTotalSalary());

            // 4-1. 복합키(Composite Key) 생성
            OcrResultId resultId = new OcrResultId(caseId, data.getCaseYear());

            // 4-2. 데이터 조회 (없으면 새로 생성)
            OcrResult ocrResult = ocrResultRepository.findById(resultId)
                    .orElseGet(() -> new OcrResult(taxCase, data.getCaseYear()));

            // 4-3. 데이터 업데이트 (DTO -> Entity)
            // 엔티티에 추가한 updateData 메서드를 사용해 값을 덮어씁니다.
            ocrResult.updateData(data);

            // 4-4. 저장
            ocrResultRepository.save(ocrResult);
        }

        // 5. [계산 단계] 환급액 계산 실행
        log.info("Case ID: {} 환급액 계산 시작...", caseId);
        refundCalculationService.calculateRefund(caseId);
        log.info("Case ID: {} 환급액 계산 완료.", caseId);
    }
}