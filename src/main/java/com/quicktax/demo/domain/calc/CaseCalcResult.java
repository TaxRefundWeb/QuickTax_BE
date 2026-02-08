package com.quicktax.demo.domain.calc;

import com.quicktax.demo.domain.refund.RefundCase;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "case_calc_result")
@Getter
@Builder // 💡 추가: 빌더 패턴 사용
@AllArgsConstructor // 💡 추가: 빌더 사용 시 필요
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CaseCalcResult {

    @EmbeddedId
    private CaseCalcResultId id;

    @MapsId("caseId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "case_id", nullable = false)
    private RefundCase refundCase;

    @Column(name = "tax_base_amount")
    private Long taxBaseAmount;

    @Column(name = "calculated_tax_rate")
    private Long calculatedTaxRate;

    @Column(name = "calculated_tax")
    private Long calculatedTax;

    @Column(name = "earned_income_amount")
    private Long earnedIncomeAmount;

    @Column(name = "youth_tax_reduction_amount")
    private Long youthTaxReductionAmount;

    @Column(name = "child_income_amount")
    private Long childIncomeAmount;

    @Column(name = "child_tax_credit_amount")
    private Long childTaxCreditAmount;

    @Column(name = "total_tax_credit_amount")
    private Long totalTaxCreditAmount;

    @Column(name = "determined_tax_amount")
    private Long determinedTaxAmount;

    @Column(name = "refund_amount")
    private Long refundAmount;

    @Column(name = "tax_difference_amount")
    private Long taxDifferenceAmount;

    @Column(name = "scenario_text", length = 200)
    private String scenarioText;

    // 생성자 (수동 생성 시 사용)
    public CaseCalcResult(
            RefundCase refundCase,
            Integer caseYear,
            String scenarioCode
    ) {
        this.refundCase = refundCase;
        this.id = new CaseCalcResultId(refundCase.getCaseId(), caseYear, scenarioCode);
    }
}