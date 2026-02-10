package com.quicktax.demo.domain.ocr;

import com.quicktax.demo.domain.cases.TaxCase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ocr_result")
@Getter
@NoArgsConstructor
public class OcrResult {

    @EmbeddedId
    private OcrResultId id;

    @MapsId("caseId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "case_id", nullable = false)
    private TaxCase taxCase;

    @Column(name = "url", columnDefinition = "text")
    private String url;

    @Column(name = "total_salary")
    private Long totalSalary;

    @Column(name = "earned_income_deduction_amount")
    private Long earnedIncomeDeductionAmount;

    @Column(name = "earned_income_amount")
    private Long earnedIncomeAmount;

    @Column(name = "basic_deduction_self_amount")
    private Long basicDeductionSelfAmount;

    @Column(name = "basic_deduction_spouse_amount")
    private Long basicDeductionSpouseAmount;

    @Column(name = "basic_deduction_dependents_amount")
    private Long basicDeductionDependentsAmount;

    @Column(name = "national_pension_deduction_amount")
    private Long nationalPensionDeductionAmount;

    @Column(name = "total_special_income_deduction_amount")
    private Long totalSpecialIncomeDeductionTotalAmount;

    @Column(name = "adjusted_income_amount")
    private Long adjustedIncomeAmount;

    @Column(name = "other_income_deduction_total_amount")
    private Long otherIncomeDeductionTotalAmount;

    @Column(name = "other_income_deduction_extra")
    private Long otherIncomeDeductionExtra;

    @Column(name = "tax_base_amount")
    private Long taxBaseAmount;

    @Column(name = "calculated_tax_amount")
    private Long calculatedTaxAmount;

    @Column(name = "tax_reduction_total_amount")
    private Long taxReductionTotalAmount;

    @Column(name = "earned_income_total_amount")
    private Long earnedIncomeTotalAmount;

    @Column(name = "eligible_children_count")
    private Long eligibleChildrenCount;

    @Column(name = "childbirth_adoption_count")
    private Long childbirthAdoptionCount;

    @Column(name = "donation_total_amount")
    private Long donationTotalAmount;

    @Column(name = "standard_tax_credit")
    private Long standardTaxCredit;

    @Column(name = "monthly_rent_tax_credit_amount")
    private Long monthlyRentTaxCreditAmount;

    @Column(name = "total_tax_credit_amount")
    private Long totalTaxCreditAmount;

    @Column(name = "determined_tax_amount_origin")
    private Long determinedTaxAmountOrigin;


    public OcrResult(TaxCase taxCase, Integer caseYear) {
        this.taxCase = taxCase;
        this.id = new OcrResultId(taxCase.getCaseId(), caseYear);
    }
}
