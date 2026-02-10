package com.quicktax.demo.service.ocr;

import com.quicktax.demo.common.ApiException;
import com.quicktax.demo.common.ErrorCode;
import com.quicktax.demo.domain.cases.TaxCase;
// import com.quicktax.demo.domain.cases.TaxCaseYear; // 💡 엔티티 경로 확인 후 주석 해제
import com.quicktax.demo.domain.ocr.OcrJob;
import com.quicktax.demo.domain.ocr.OcrJobStatus;
import com.quicktax.demo.dto.OcrConfirmRequest;
import com.quicktax.demo.dto.OcrConfirmRequest.OcrYearData;
import com.quicktax.demo.repo.OcrJobRepository;
import com.quicktax.demo.repo.TaxCaseRepository;
// import com.quicktax.demo.repo.TaxCaseYearRepository; // 💡 리포지토리 경로 확인 후 주석 해제
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
    private final OcrJobRepository ocrJobRepository; // 💡 상태 확인을 위해 추가
    private final RefundResultService refundCalculationService; // 계산 서비스

    // 💡 [TODO] 팀원의 리포지토리 코드가 머지되면 주석을 해제하고 생성자를 주입받으세요.
    // private final TaxCaseYearRepository taxCaseYearRepository;

    /**
     * OCR 확정 및 환급액 계산 요청 처리
     * 1. Case 및 권한 검증
     * 2. OCR 완료 상태 검증 (완료되지 않았으면 409 리턴)
     * 3. DB에 수정된 OCR 데이터 저장
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

        // 3. OCR 작업 상태 확인 (409 Conflict 체크)
        OcrJob ocrJob = ocrJobRepository.findById(caseId)
                .orElseThrow(() -> new ApiException(ErrorCode.COMMON404, "OCR 요청 내역이 존재하지 않습니다."));

        // 상태가 READY(완료)가 아니라면 에러 발생
        if (ocrJob.getStatus() != OcrJobStatus.READY) {
            // 💡 ErrorCode에 COMMON409가 정의되어 있어야 합니다.
            throw new ApiException(ErrorCode.OCR409, "OCR 분석이 아직 완료되지 않았습니다. 잠시 후 다시 시도해주세요.");
        }

        // 4. [저장 단계] 요청받은 연도별 데이터를 DB에 업데이트 (덮어쓰기)
        for (OcrYearData data : request.getOcrData()) {
            log.info("OCR 확정 데이터 저장 중... 연도: {}, 총급여: {}", data.getCaseYear(), data.getTotalSalary());

            // 💡 [TODO] 팀원의 엔티티(TaxCaseYear)가 준비되면 아래 주석을 풀고 사용하세요.
            /*
            // 4-1. 해당 연도의 데이터 조회 (없으면 생성)
            TaxCaseYear caseYear = taxCaseYearRepository.findByTaxCaseAndYear(taxCase, data.getCaseYear())
                    .orElseGet(() -> TaxCaseYear.builder()
                            .taxCase(taxCase)
                            .year(data.getCaseYear())
                            .build());

            // 4-2. 데이터 업데이트
            caseYear.setTotalSalary(data.getTotalSalary());
            caseYear.setEarnedIncomeDeduction(data.getEarnedIncomeDeduction());
            caseYear.setEarnedIncomeAmount(data.getEarnedIncomeAmount());
            caseYear.setBasicDeductionSelf(data.getBasicDeductionSelf());
            caseYear.setBasicDeductionSpouse(data.getBasicDeductionSpouse());
            caseYear.setBasicDeductionDependents(data.getBasicDeductionDependents());
            caseYear.setNationalPensionDeduction(data.getNationalPensionDeduction());
            caseYear.setTotalSpecialIncomeDeduction(data.getTotalSpecialIncomeDeduction());
            caseYear.setAdjustedIncomeAmount(data.getAdjustedIncomeAmount());
            caseYear.setOtherIncomeDeductionTotal(data.getOtherIncomeDeductionTotal());
            caseYear.setTaxBaseAmount(data.getTaxBaseAmount());
            caseYear.setCalculatedTaxAmount(data.getCalculatedTaxAmount());
            caseYear.setTaxReductionTotal(data.getTaxReductionTotal());
            caseYear.setEarnedIncomeTotal(data.getEarnedIncomeTotal());
            caseYear.setEligibleChildrenCount(data.getEligibleChildrenCount());
            caseYear.setChildbirthAdoptionCount(data.getChildbirthAdoptionCount());
            caseYear.setMonthlyRentTaxCredit(data.getMonthlyRentTaxCredit());
            caseYear.setTotalTaxCredit(data.getTotalTaxCredit());
            caseYear.setDeterminedTaxAmount(data.getDeterminedTaxAmount());

            // 4-3. 저장
            taxCaseYearRepository.save(caseYear);
            */
        }

        // 데이터 반영 (Flush) - 계산 로직에서 최신 데이터를 읽기 위함
        // if (taxCaseYearRepository != null) {
        //     taxCaseYearRepository.flush();
        // }

        // 5. [계산 단계] 환급액 계산 실행
        log.info("Case ID: {} 환급액 계산 시작...", caseId);
        refundCalculationService.calculateRefund(caseId);
        log.info("Case ID: {} 환급액 계산 완료.", caseId);
    }
}