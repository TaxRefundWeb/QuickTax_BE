package com.quicktax.demo.service.ocr;

import com.quicktax.demo.common.ApiException;
import com.quicktax.demo.common.ErrorCode;
import com.quicktax.demo.domain.cases.TaxCase;
import com.quicktax.demo.domain.ocr.OcrJob;
import com.quicktax.demo.domain.ocr.OcrJobStatus;
import com.quicktax.demo.domain.ocr.OcrResult;
import com.quicktax.demo.domain.ocr.OcrResultPerCompany;
import com.quicktax.demo.dto.ocr.OcrDataResponse;
import com.quicktax.demo.repo.TaxCaseRepository;
import com.quicktax.demo.repo.ocr.OcrJobRepository;
import com.quicktax.demo.repo.ocr.OcrResultPerCompanyRepository;
import com.quicktax.demo.repo.ocr.OcrResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OcrQueryService {

    private final TaxCaseRepository taxCaseRepository;
    private final OcrJobRepository ocrJobRepository;
    private final OcrResultRepository ocrResultRepository;
    private final OcrResultPerCompanyRepository ocrResultPerCompanyRepository;

    private void requireLogin(Long cpaId) {
        if (cpaId == null) throw new ApiException(ErrorCode.AUTH401, "로그인이 필요합니다.");
    }

    private void requireOwnedCase(Long cpaId, Long caseId) {
        TaxCase taxCase = taxCaseRepository.findById(caseId)
                .orElseThrow(() -> new ApiException(ErrorCode.COMMON404, "존재하지 않는 caseId 입니다."));

        Long ownerCpaId = taxCase.getCustomer().getTaxCompany().getCpaId();
        if (!cpaId.equals(ownerCpaId)) throw new ApiException(ErrorCode.AUTH403, "권한이 없습니다.");
    }

    public OcrDataResponse getOcr(Long cpaId, Long caseId) {
        requireLogin(cpaId);
        requireOwnedCase(cpaId, caseId);

        OcrJob job = ocrJobRepository.findById(caseId)
                .orElseThrow(() -> new ApiException(ErrorCode.COMMON404, "ocr_job 없음: presign 먼저"));

        // READY 아니면 상태만 내려주고 data=null
        if (job.getStatus() != OcrJobStatus.READY) {
            return new OcrDataResponse(job.getStatus(), job.getErrorCode(), job.getErrorMessage(), null);
        }

        List<OcrResult> results = ocrResultRepository.findByIdCaseIdOrderByIdCaseYearAsc(caseId);
        List<OcrResultPerCompany> perCompanies = ocrResultPerCompanyRepository.findByCaseIdOrderByYearCompany(caseId);

        Map<Integer, List<OcrDataResponse.CompanySalary>> companiesByYear = new HashMap<>();
        for (OcrResultPerCompany p : perCompanies) {
            int year = p.getId().getOcrResultId().getCaseYear();
            companiesByYear.computeIfAbsent(year, k -> new ArrayList<>())
                    .add(new OcrDataResponse.CompanySalary(p.getId().getCompanyId(), p.getSalary()));
        }

        List<OcrDataResponse.OcrYearResult> data = new ArrayList<>();
        for (OcrResult r : results) {
            int year = r.getId().getCaseYear();
            List<OcrDataResponse.CompanySalary> companies = companiesByYear.getOrDefault(year, List.of());

            data.add(new OcrDataResponse.OcrYearResult(
                    year,
                    r.getUrl(),

                    r.getTotalSalary(),
                    r.getEarnedIncomeDeductionAmount(),
                    r.getEarnedIncomeAmount(),

                    r.getBasicDeductionSelfAmount(),
                    r.getBasicDeductionSpouseAmount(),
                    r.getBasicDeductionDependentsAmount(),

                    r.getNationalPensionDeductionAmount(),
                    r.getTotalSpecialIncomeDeductionTotalAmount(),

                    r.getAdjustedIncomeAmount(),

                    r.getOtherIncomeDeductionTotalAmount(),
                    r.getOtherIncomeDeductionExtra(),

                    r.getTaxBaseAmount(),
                    r.getCalculatedTaxAmount(),
                    r.getTaxReductionTotalAmount(),

                    r.getEarnedIncomeTotalAmount(),

                    r.getEligibleChildrenCount(),
                    r.getChildbirthAdoptionCount(),

                    r.getDonationTotalAmount(),

                    r.getStandardTaxCredit(),
                    r.getMonthlyRentTaxCreditAmount(),
                    r.getTotalTaxCreditAmount(),

                    r.getDeterminedTaxAmountOrigin(),

                    companies
            ));
        }

        return new OcrDataResponse(job.getStatus(), job.getErrorCode(), job.getErrorMessage(), data);
    }


}
