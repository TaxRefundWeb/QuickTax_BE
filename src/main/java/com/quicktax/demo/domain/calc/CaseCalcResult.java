package com.quicktax.demo.domain.calc;

import com.quicktax.demo.domain.cases.TaxCase;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "case_calc_result")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CaseCalcResult {

    @EmbeddedId
    private CaseCalcResultId id;

    @MapsId("caseId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "case_id", nullable = false)
    private TaxCase taxCase;

    @Column(name = "tax_base_amount")
    private Long taxBaseAmount;

    @Column(name = "calculated_tax_rate")
    private Long calculatedTaxRate;

    @Column(name = "earned_income_amount")
    private Long earnedIncomeAmount;

    @Column(name = "youth_tax_reduction_amount")
    private Long youthTaxReductionAmount;

    @Column(name = "child_income_amount")
    private Long childIncomeAmount;

    @Column(name = "child_tax_credit_amount")
    private Long childTaxCreditAmount;

    @Column(name = "spouse_tax_credit_amount")
    private Long spouseTaxCreditAmount;

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

    // 기존에 있던 기본 생성자 (ID만 생성)
    public CaseCalcResult(TaxCase taxCase, Integer caseYear, String scenarioCode) {
        this.taxCase = taxCase;
        this.id = new CaseCalcResultId(taxCase.getCaseId(), caseYear, scenarioCode);
    }

    // ✅ [추가됨] 전체 필드를 저장하기 위한 Builder 패턴 생성자
    // 이 코드는 DB 스키마에 전혀 영향을 주지 않으며, 오직 자바 서비스에서 객체를 생성할 때 사용됩니다.
    @Builder
    public CaseCalcResult(
            TaxCase taxCase,
            Integer caseYear,
            String scenarioCode,
            Long taxBaseAmount,
            Long calculatedTaxRate,
            Long earnedIncomeAmount,
            Long youthTaxReductionAmount,
            Long childIncomeAmount,
            Long childTaxCreditAmount,
            Long spouseTaxCreditAmount,
            Long totalTaxCreditAmount,
            Long determinedTaxAmount,
            Long refundAmount,
            Long taxDifferenceAmount,
            String scenarioText
    ) {
        this.taxCase = taxCase;
        // 복합키 생성 (caseId, caseYear, scenarioCode)
        this.id = new CaseCalcResultId(taxCase.getCaseId(), caseYear, scenarioCode);

        // 데이터 매핑
        this.taxBaseAmount = taxBaseAmount;
        this.calculatedTaxRate = calculatedTaxRate;
        this.earnedIncomeAmount = earnedIncomeAmount;
        this.youthTaxReductionAmount = youthTaxReductionAmount;
        this.childIncomeAmount = childIncomeAmount;
        this.childTaxCreditAmount = childTaxCreditAmount;
        this.spouseTaxCreditAmount = spouseTaxCreditAmount;
        this.totalTaxCreditAmount = totalTaxCreditAmount;
        this.determinedTaxAmount = determinedTaxAmount;
        this.refundAmount = refundAmount;
        this.taxDifferenceAmount = taxDifferenceAmount;
        this.scenarioText = scenarioText;
    }
}