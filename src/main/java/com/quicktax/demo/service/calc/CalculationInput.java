package com.quicktax.demo.service.calc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalculationInput {

    // 감면 대상 여부
    private boolean reductionYn;

    // 배우자 유무
    private boolean spouseYn;

    // 자녀 유무
    private boolean childYn;

    // 근무처 수
    private int companyCount;

    // 배우자 공제액 (금액이므로 Long 권장)
    private Long basicDeductionSpouseAmount;

    // 부양가족 공제액
    private Long basicDeductionDependentsAmount;

    // 총 세액감면 금액
    private Long taxReductionTotalAmount;

    // 경정청구 신청일
    private LocalDate claimDate;

    // 감면 종료일
    private LocalDate reductionEnd;
}