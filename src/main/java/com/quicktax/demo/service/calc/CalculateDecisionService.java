package com.quicktax.demo.service.calc;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CalculateDecisionService {

    /**
     * 입력값을 분석하여 적용 가능한 시나리오(ScenarioCode) 목록을 반환
     */
    public List<ScenarioCode> decide(CalculationInput input) {

        List<ScenarioCode> result = new ArrayList<>();

        boolean dateValid = isDateValid(input);
        boolean hasFamily = input.isSpouseYn() || input.isChildYn(); // 배우자 또는 자녀 있음

        // 인적공제(기본공제)를 이미 받았는지 확인
        boolean hasBasicDeduction = input.getBasicDeductionSpouseAmount() != null
                || input.getBasicDeductionDependentsAmount() != null;

        /* =========================
           1. 자녀 감면 (날짜 무시)
           ========================= */
        if (hasFamily) {
            // 원본 로직 유지: 가족이 있으면 family_reduction
            result.add(ScenarioCode.family_reduction);
        }

        /* =========================
           2. 감면 이후 방식 (기한 후)
           ========================= */
        if (input.isReductionYn()) {
            result.add(ScenarioCode.timepast_reduction);
        }

        /* =========================
           날짜 조건 + 감면 여부 체크 (여기서 조건 안 맞으면 종료)
           ========================= */
        if (!input.isReductionYn() || !dateValid) {
            return result;
        }

        /* =========================
           3. 청년 감면
           ========================= */
        if (input.getCompanyCount() == 1) {
            // 원본 로직 유지: 청년 감면도 코드상으로는 family_reduction을 사용하는 것으로 보임
            // (만약 CHILD_REDUCTION을 써야 한다면 여기를 ScenarioCode.CHILD_REDUCTION 으로 변경하세요)
            result.add(ScenarioCode.family_reduction);
        }

        /* =========================
           4. 이중근로
           ========================= */
        if (input.getCompanyCount() >= 2) {
            result.add(ScenarioCode.double_reduction);
        }

        /* =========================
           5. 혼합 계산 (청년 + 자녀)
           ========================= */
        if (input.getCompanyCount() >= 2 && hasFamily) {
            result.add(ScenarioCode.mix_reduction);
        }

        /* =========================
           6. 청년 감면 추가 (자녀 완료 후 청년 추가)
           ========================= */
        if (input.getCompanyCount() >= 1
                && hasBasicDeduction
                && input.getTaxReductionTotalAmount() == null) {

            result.add(ScenarioCode.youthplus_reduction);
        }

        /* =========================
           7. 자녀 추가 감면 (청년 완료 후 자녀 추가)
           ========================= */
        if (!hasBasicDeduction
                && input.getTaxReductionTotalAmount() != null) {

            result.add(ScenarioCode.familyplus_reduction);
        }

        return result;
    }

    // 날짜 유효성 검사
    private boolean isDateValid(CalculationInput input) {
        if (input.getClaimDate() == null || input.getReductionEnd() == null) {
            return false;
        }
        return !input.getClaimDate().isAfter(input.getReductionEnd());
    }
}