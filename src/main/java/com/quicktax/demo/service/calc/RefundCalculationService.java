package com.quicktax.demo.service.calc;

import com.quicktax.demo.common.ApiException;
import com.quicktax.demo.common.ErrorCode;
import com.quicktax.demo.domain.calc.CaseCalcResult;
import com.quicktax.demo.domain.calc.CaseCalcResultId;
import com.quicktax.demo.domain.refund.RefundCase;
import com.quicktax.demo.repo.CaseCalcResultRepository;
import com.quicktax.demo.repo.RefundCaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RefundCalculationService {

    private final RefundCaseRepository refundCaseRepository;
    private final CaseCalcResultRepository caseCalcResultRepository;

    /**
     * 경정청구 세액 계산 및 저장 (메인 로직)
     */
    @Transactional
    public void calculateRefund(Long caseId) {
        // 1. Case 조회
        RefundCase refundCase = refundCaseRepository.findById(caseId)
                .orElseThrow(() -> new ApiException(ErrorCode.BADREQ400, "존재하지 않는 Case ID입니다."));

        // 2. 기존 계산 결과 삭제 (재계산 시 중복 방지)
        List<CaseCalcResult> oldResults = caseCalcResultRepository.findAllByCaseId(caseId);
        if (!oldResults.isEmpty()) {
            caseCalcResultRepository.deleteAll(oldResults);
        }

        // 3. 계산 대상 연도 파싱
        int startYear = parseYear(refundCase.getClaimStart());
        int endYear = parseYear(refundCase.getClaimEnd());

        List<CaseCalcResult> newResults = new ArrayList<>();

        // 4. 연도별 계산 루프 (현재는 더미 데이터 생성)
        for (int year = startYear; year <= endYear; year++) {

            // TODO: 실제 계산 로직 구현 필요
            // 1) User의 연소득 정보 조회
            // 2) 감면 대상 여부 판단
            // 3) 세액 계산

            // [테스트용] 청년 경정청구 더미 데이터
            newResults.add(createDummyResult(refundCase, year, "청년 경정청구 신청"));

            // [테스트용] 짝수 해에는 자녀 경정청구도 추가
            if (year % 2 == 0) {
                newResults.add(createDummyResult(refundCase, year, "자녀 경정청구 신청"));
            }
        }

        // 5. 결과 DB 저장
        caseCalcResultRepository.saveAll(newResults);
        System.out.println("Case " + caseId + " 계산 완료: " + newResults.size() + "건 저장됨.");
    }

    private int parseYear(String dateStr) {
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE).getYear();
        } catch (Exception e) {
            // 날짜 파싱 실패 시 기본값 혹은 에러 처리 (여기선 현재 연도 - 1)
            return LocalDate.now().getYear() - 1;
        }
    }

    // 껍데기용 더미 데이터 생성 메서드
    private CaseCalcResult createDummyResult(RefundCase refundCase, int year, String scenarioCode) {
        return CaseCalcResult.builder()
                .id(new CaseCalcResultId(refundCase.getCaseId(), year, scenarioCode))
                .refundCase(refundCase)
                .taxBaseAmount(35000000L)       // 과세표준
                .calculatedTaxRate(15L)         // 세율(%)
                .calculatedTax(5250000L)        // 산출세액 (금액)
                .determinedTaxAmount(3000000L)  // 결정세액
                .refundAmount(2250000L)         // 환급액
                .taxDifferenceAmount(2250000L)  // 세액차이
                .earnedIncomeAmount(50000000L)  // 근로소득
                .scenarioText(scenarioCode + " - (자동 계산 테스트)")
                .build();
    }
}