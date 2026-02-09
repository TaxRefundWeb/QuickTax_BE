package com.quicktax.demo.domain.calc;

import com.quicktax.demo.domain.cases.TaxCase;
import jakarta.persistence.*;
import lombok.AccessLevel;
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


    public CaseCalcResult(TaxCase taxCase, Integer caseYear, String scenarioCode) {
        this.taxCase = taxCase;
        this.id = new CaseCalcResultId(taxCase.getCaseId(), caseYear, scenarioCode);
    }
}
