package com.quicktax.demo.domain.ocr;

import com.quicktax.demo.domain.cases.TaxCase;
import com.quicktax.demo.dto.OcrConfirmRequest.OcrYearData;
import com.quicktax.demo.ocr.OcrNumberSanitizer;
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

    public void updateData(OcrYearData data) {
        this.totalSalary = data.getTotalSalary();
        this.earnedIncomeDeductionAmount = data.getEarnedIncomeDeduction();
        this.earnedIncomeAmount = data.getEarnedIncomeAmount();
        this.basicDeductionSelfAmount = data.getBasicDeductionSelf();
        this.basicDeductionSpouseAmount = data.getBasicDeductionSpouse();
        this.basicDeductionDependentsAmount = data.getBasicDeductionDependents();
        this.nationalPensionDeductionAmount = data.getNationalPensionDeduction();
        this.totalSpecialIncomeDeductionTotalAmount = data.getTotalSpecialIncomeDeduction();
        this.adjustedIncomeAmount = data.getAdjustedIncomeAmount();
        this.otherIncomeDeductionTotalAmount = data.getOtherIncomeDeductionTotal();
        this.otherIncomeDeductionExtra = data.getOtherIncomeDeductionExtra();
        this.taxBaseAmount = data.getTaxBaseAmount();
        this.calculatedTaxAmount = data.getCalculatedTaxAmount();
        this.taxReductionTotalAmount = data.getTaxReductionTotal();
        this.earnedIncomeTotalAmount = data.getEarnedIncomeTotal();
        this.eligibleChildrenCount = data.getEligibleChildrenCount() != null ? data.getEligibleChildrenCount().longValue() : 0L;
        this.childbirthAdoptionCount = data.getChildbirthAdoptionCount() != null ? data.getChildbirthAdoptionCount().longValue() : 0L;
        this.donationTotalAmount = data.getDonationTotalAmount();
        this.standardTaxCredit = data.getStandardTaxCredit();
        this.monthlyRentTaxCreditAmount = data.getMonthlyRentTaxCredit();
        this.totalTaxCreditAmount = data.getTotalTaxCredit();
        this.determinedTaxAmountOrigin = data.getDeterminedTaxAmount();
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void applyTemplateNumbers(java.util.Map<String, String> fields) {
        if (fields == null) return;

        this.totalSalary = n(fields, "total_salary", "totalSalary");
        this.earnedIncomeDeductionAmount = n(fields, "earned_income_deduction_amount", "earnedIncomeDeductionAmount");
        this.earnedIncomeAmount = n(fields, "earned_income_amount", "earnedIncomeAmount");

        this.basicDeductionSelfAmount = n(fields, "basic_deduction_self_amount", "basicDeductionSelfAmount");
        this.basicDeductionSpouseAmount = n(fields, "basic_deduction_spouse_amount", "basicDeductionSpouseAmount");
        this.basicDeductionDependentsAmount = n(fields, "basic_deduction_dependents_amount", "basicDeductionDependentsAmount");

        this.nationalPensionDeductionAmount = n(fields, "national_pension_deduction_amount", "nationalPensionDeductionAmount");
        this.totalSpecialIncomeDeductionTotalAmount = n(fields, "total_special_income_deduction_amount", "totalSpecialIncomeDeductionTotalAmount");
        this.adjustedIncomeAmount = n(fields, "adjusted_income_amount", "adjustedIncomeAmount");

        this.otherIncomeDeductionTotalAmount = n(fields, "other_income_deduction_total_amount", "otherIncomeDeductionTotalAmount");
        this.otherIncomeDeductionExtra = n(fields, "other_income_deduction_extra", "otherIncomeDeductionExtra");

        this.taxBaseAmount = n(fields, "tax_base_amount", "taxBaseAmount");
        this.calculatedTaxAmount = n(fields, "calculated_tax_amount", "calculatedTaxAmount");
        this.taxReductionTotalAmount = n(fields, "tax_reduction_total_amount", "taxReductionTotalAmount");
        this.earnedIncomeTotalAmount = n(fields, "earned_income_total_amount", "earnedIncomeTotalAmount");

        this.eligibleChildrenCount = n(fields, "eligible_children_count", "eligibleChildrenCount");
        this.childbirthAdoptionCount = n(fields, "childbirth_adoption_count", "childbirthAdoptionCount");

        this.donationTotalAmount = n(fields, "donation_total_amount", "donationTotalAmount");
        this.standardTaxCredit = n(fields, "standard_tax_credit", "standardTaxCredit");

        this.monthlyRentTaxCreditAmount = n(fields, "monthly_rent_tax_credit_amount", "monthlyRentTaxCreditAmount");
        this.totalTaxCreditAmount = n(fields, "total_tax_credit_amount", "totalTaxCreditAmount");

        this.determinedTaxAmountOrigin = n(fields, "determined_tax_amount_origin", "determinedTaxAmountOrigin", "determined_tax_amount");
    }

    public void addTemplateNumbers(java.util.Map<String, String> fields) {
        if (fields == null) return;

        this.totalSalary = add(this.totalSalary, n(fields, "total_salary", "totalSalary"));
        this.earnedIncomeDeductionAmount = add(this.earnedIncomeDeductionAmount, n(fields, "earned_income_deduction_amount", "earnedIncomeDeductionAmount"));
        this.earnedIncomeAmount = add(this.earnedIncomeAmount, n(fields, "earned_income_amount", "earnedIncomeAmount"));

        this.basicDeductionSelfAmount = add(this.basicDeductionSelfAmount, n(fields, "basic_deduction_self_amount", "basicDeductionSelfAmount"));
        this.basicDeductionSpouseAmount = add(this.basicDeductionSpouseAmount, n(fields, "basic_deduction_spouse_amount", "basicDeductionSpouseAmount"));
        this.basicDeductionDependentsAmount = add(this.basicDeductionDependentsAmount, n(fields, "basic_deduction_dependents_amount", "basicDeductionDependentsAmount"));

        this.nationalPensionDeductionAmount = add(this.nationalPensionDeductionAmount, n(fields, "national_pension_deduction_amount", "nationalPensionDeductionAmount"));
        this.totalSpecialIncomeDeductionTotalAmount = add(this.totalSpecialIncomeDeductionTotalAmount, n(fields, "total_special_income_deduction_amount", "totalSpecialIncomeDeductionTotalAmount"));
        this.adjustedIncomeAmount = add(this.adjustedIncomeAmount, n(fields, "adjusted_income_amount", "adjustedIncomeAmount"));

        this.otherIncomeDeductionTotalAmount = add(this.otherIncomeDeductionTotalAmount, n(fields, "other_income_deduction_total_amount", "otherIncomeDeductionTotalAmount"));
        this.otherIncomeDeductionExtra = add(this.otherIncomeDeductionExtra, n(fields, "other_income_deduction_extra", "otherIncomeDeductionExtra"));

        this.taxBaseAmount = add(this.taxBaseAmount, n(fields, "tax_base_amount", "taxBaseAmount"));
        this.calculatedTaxAmount = add(this.calculatedTaxAmount, n(fields, "calculated_tax_amount", "calculatedTaxAmount"));
        this.taxReductionTotalAmount = add(this.taxReductionTotalAmount, n(fields, "tax_reduction_total_amount", "taxReductionTotalAmount"));
        this.earnedIncomeTotalAmount = add(this.earnedIncomeTotalAmount, n(fields, "earned_income_total_amount", "earnedIncomeTotalAmount"));

        this.eligibleChildrenCount = add(this.eligibleChildrenCount, n(fields, "eligible_children_count", "eligibleChildrenCount"));
        this.childbirthAdoptionCount = add(this.childbirthAdoptionCount, n(fields, "childbirth_adoption_count", "childbirthAdoptionCount"));

        this.donationTotalAmount = add(this.donationTotalAmount, n(fields, "donation_total_amount", "donationTotalAmount"));
        this.standardTaxCredit = add(this.standardTaxCredit, n(fields, "standard_tax_credit", "standardTaxCredit"));

        this.monthlyRentTaxCreditAmount = add(this.monthlyRentTaxCreditAmount, n(fields, "monthly_rent_tax_credit_amount", "monthlyRentTaxCreditAmount"));
        this.totalTaxCreditAmount = add(this.totalTaxCreditAmount, n(fields, "total_tax_credit_amount", "totalTaxCreditAmount"));

        this.determinedTaxAmountOrigin = add(this.determinedTaxAmountOrigin, n(fields, "determined_tax_amount_origin", "determinedTaxAmountOrigin", "determined_tax_amount"));
    }

    private static Long n(java.util.Map<String, String> fields, String... keys) {
        String v = pick(fields, keys);
        return OcrNumberSanitizer.toLongOrNull(v);
    }

    private static String pick(java.util.Map<String, String> fields, String... keys) {
        for (String k : keys) {
            if (k == null) continue;
            String v = fields.get(k);
            if (v != null && !v.isBlank()) return v;
        }
        return null;
    }

    private static Long add(Long base, Long inc) {
        long b = base == null ? 0L : base;
        long i = inc == null ? 0L : inc;
        return b + i;
    }
}
