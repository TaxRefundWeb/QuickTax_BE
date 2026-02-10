package com.quicktax.demo.dto.ocr;

import com.quicktax.demo.domain.ocr.OcrJobStatus;
import java.util.List;

public record OcrDataResponse(
        OcrJobStatus status,
        String errorCode,
        String errorMessage,
        List<OcrYearResult> data
) {
    public record OcrYearResult(
            int caseYear,
            String url,

            Long totalSalary,
            Long earnedIncomeDeductionAmount,
            Long earnedIncomeAmount,

            Long basicDeductionSelfAmount,
            Long basicDeductionSpouseAmount,
            Long basicDeductionDependentsAmount,

            Long nationalPensionDeductionAmount,
            Long totalSpecialIncomeDeductionTotalAmount,

            Long adjustedIncomeAmount,

            Long otherIncomeDeductionTotalAmount,
            Long otherIncomeDeductionExtra,

            Long taxBaseAmount,
            Long calculatedTaxAmount,
            Long taxReductionTotalAmount,

            Long earnedIncomeTotalAmount,

            Long eligibleChildrenCount,
            Long childbirthAdoptionCount,

            Long donationTotalAmount,

            Long standardTaxCredit,
            Long monthlyRentTaxCreditAmount,
            Long totalTaxCreditAmount,

            Long determinedTaxAmountOrigin,

            List<CompanySalary> companies
    ) {}

    public record CompanySalary(
            int companyId,
            Long salary
    ) {}
}
