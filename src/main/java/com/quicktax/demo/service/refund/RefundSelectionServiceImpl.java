package com.quicktax.demo.service.refund;

import com.quicktax.demo.common.ApiException;
import com.quicktax.demo.common.ErrorCode;
import com.quicktax.demo.domain.cases.TaxCase;
import com.quicktax.demo.domain.cases.draft.*;
import com.quicktax.demo.domain.ocr.OcrJob;
import com.quicktax.demo.dto.refund.*;
import com.quicktax.demo.repo.ocr.OcrJobRepository;
import com.quicktax.demo.repo.TaxCaseRepository;
import com.quicktax.demo.repo.draft.*;
import com.quicktax.demo.service.customer.CustomerService;
import com.quicktax.demo.service.s3.OcrS3KeyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class RefundSelectionServiceImpl implements RefundSelectionService {

    private final CustomerService customerService;
    private final TaxCaseRepository taxCaseRepository;

    private final CaseDraftYearRepository caseDraftYearRepository;
    private final CaseDraftYearCompanyRepository companyRepository;
    private final CaseDraftYearSpouseRepository spouseRepository;
    private final CaseDraftYearChildRepository childRepository;

    private final OcrJobRepository ocrJobRepository;
    private final OcrS3KeyService keyService;

    /**
     * 1) 경정청구 기간 선택 → cases 생성 (+ ocr_job도 같이 생성)
     */
    @Override
    public Long createCase(Long cpaId, Long customerId, RefundSelectionRequest request) {
        try {
            // 권한 체크 (403)
            var customer = customerService.checkCustomerOwnership(cpaId, customerId);

            // 기본 검증
            if (request.claim_date() == null) throw new ApiException(ErrorCode.BADREQ400, "claim_date는 필수입니다.");
            if (request.claim_from() > request.claim_to()) throw new ApiException(ErrorCode.BADREQ400, "claim_from <= claim_to 여야 합니다.");

            LocalDate reductionStart = request.reduction_start();
            LocalDate reductionEnd = request.reduction_end();


            // 1) cases 저장
            TaxCase taxCase = new TaxCase(customer);
            taxCase.applySelection(
                    request.claim_from(),
                    request.claim_to(),
                    reductionStart,
                    reductionEnd,
                    request.claim_date()
            );

            taxCaseRepository.saveAndFlush(taxCase); // ✅ IDENTITY면 이게 핵심
            Long caseId = taxCase.getCaseId();
            if (caseId == null) throw new ApiException(ErrorCode.COMMON500, "case_id 생성 실패");

            // 2) ocr_job 생성 (신규 케이스니까 그냥 생성이 정답)
            String key = keyService.rawPdfKey(caseId);

            OcrJob job = new OcrJob(taxCase);
            job.resetWaitingUpload(key);

            ocrJobRepository.saveAndFlush(job);

            return caseId;

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            // ✅ swagger 응답에 “진짜 원인”이 박히게 함
            throw new ApiException(
                    ErrorCode.COMMON500,
                    "refund-selection 실패: " + e.getClass().getSimpleName() + " - " + (e.getMessage() == null ? "" : e.getMessage())
            );
        }
    }


    /**
     * 2) 경정청구 신청 정보 입력 → case_draft_* 저장
     */
    @Override
    public RefundClaimResponse saveRefundClaims(Long cpaId, Long caseId, RefundClaimRequest request) {

        // case 존재 + 권한 체크
        TaxCase taxCase = taxCaseRepository.findById(caseId)
                .orElseThrow(() -> new ApiException(ErrorCode.COMMON404, "존재하지 않는 caseId 입니다."));

        Long ownerCpaId = taxCase.getCustomer().getTaxCompany().getCpaId();
        if (cpaId == null || !ownerCpaId.equals(cpaId)) {
            throw new ApiException(ErrorCode.AUTH403, "접근 권한이 없습니다.");
        }

        if (request == null || request.cases() == null || request.cases().isEmpty()) {
            throw new ApiException(ErrorCode.BADREQ400, "cases는 1개 이상 필요합니다.");
        }

        // case_year 중복 방지
        Set<Integer> yearSet = new HashSet<>();
        for (RefundYearCase yc : request.cases()) {
            if (!yearSet.add(yc.case_year())) {
                throw new ApiException(ErrorCode.BADREQ400, "case_year가 중복되었습니다: " + yc.case_year());
            }
        }

        List<Integer> savedYears = new ArrayList<>();

        for (RefundYearCase yearCase : request.cases()) {

            // 회사 최소 1개 + 최대 3개
            if (yearCase.companies() == null || yearCase.companies().isEmpty()) {
                throw new ApiException(ErrorCode.BADREQ400, "companies는 1개 이상 필요합니다. case_year=" + yearCase.case_year());
            }
            if (yearCase.companies().size() > 3) {
                throw new ApiException(ErrorCode.BADREQ400, "companies는 최대 3개까지 가능합니다. case_year=" + yearCase.case_year());
            }

            // draft year 저장 (이미 있으면 400)
            CaseDraftYearId yearId = new CaseDraftYearId(caseId, yearCase.case_year());
            if (caseDraftYearRepository.existsById(yearId)) {
                throw new ApiException(ErrorCode.BADREQ400, "이미 입력된 case_year 입니다: " + yearCase.case_year());
            }

            CaseDraftYear draftYear = new CaseDraftYear(
                    taxCase,
                    yearCase.case_year(),
                    yearCase.spouse_yn(),
                    yearCase.child_yn(),
                    yearCase.reduction_yn()
            );
            caseDraftYearRepository.save(draftYear);

            // companies 저장 (case_company는 1부터)
            int companyIdx = 1;
            for (RefundCompany company : yearCase.companies()) {
                if (company.case_work_start() == null) {
                    throw new ApiException(ErrorCode.BADREQ400, "case_work_start는 필수입니다. case_year=" + yearCase.case_year());
                }
                companyRepository.save(new CaseDraftYearCompany(
                        draftYear,
                        companyIdx++,
                        company.case_work_start(),
                        company.case_work_end(),
                        company.business_number(),
                        company.small_business_yn()
                ));
            }

            // spouse 저장
            if (yearCase.spouse_yn()) {
                if (yearCase.spouse() == null) {
                    throw new ApiException(ErrorCode.BADREQ400, "spouse_yn=true면 spouse는 필수입니다. case_year=" + yearCase.case_year());
                }
                spouseRepository.save(new CaseDraftYearSpouse(
                        draftYear,
                        yearCase.spouse().spouse_name(),
                        yearCase.spouse().spouse_rrn()
                ));
            } else {
                if (yearCase.spouse() != null) {
                    throw new ApiException(ErrorCode.BADREQ400, "spouse_yn=false면 spouse는 null이어야 합니다. case_year=" + yearCase.case_year());
                }
            }

            // children 저장
            if (yearCase.child_yn()) {
                if (yearCase.children() == null || yearCase.children().isEmpty()) {
                    throw new ApiException(ErrorCode.BADREQ400, "child_yn=true면 children은 1명 이상 필요합니다. case_year=" + yearCase.case_year());
                }
                int childIdx = 1;
                for (RefundChild child : yearCase.children()) {
                    childRepository.save(new CaseDraftYearChild(
                            draftYear,
                            childIdx++,
                            child.child_rrn(),
                            child.child_name()
                    ));
                }
            } else {
                if (yearCase.children() != null && !yearCase.children().isEmpty()) {
                    throw new ApiException(ErrorCode.BADREQ400, "child_yn=false면 children은 비어있어야 합니다. case_year=" + yearCase.case_year());
                }
            }

            savedYears.add(yearCase.case_year());
        }

        return new RefundClaimResponse(savedYears);
    }
}
