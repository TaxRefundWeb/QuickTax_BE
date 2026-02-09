package com.quicktax.demo.service.refund;

import com.quicktax.demo.common.ApiException;
import com.quicktax.demo.common.ErrorCode;
import com.quicktax.demo.domain.cases.TaxCase;
import com.quicktax.demo.domain.cases.draft.*;
import com.quicktax.demo.dto.refund.*;
import com.quicktax.demo.repo.TaxCaseRepository;
import com.quicktax.demo.repo.draft.*;
import com.quicktax.demo.service.customer.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

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

    /**
     * 1) 경정청구 기간 선택 → cases 생성
     */
    @Override
    public Long createCase(Long cpaId, Long customerId, RefundSelectionRequest request) {

        // 권한 체크 (403)
        var customer = customerService.checkCustomerOwnership(cpaId, customerId);

        // 기본 검증
        if (request.claim_date() == null) throw new ApiException(ErrorCode.BADREQ400, "claim_date는 필수입니다.");
        if (request.claim_from() > request.claim_to()) throw new ApiException(ErrorCode.BADREQ400, "claim_from <= claim_to 여야 합니다.");

        LocalDate reductionStart = request.reduction_start();
        LocalDate reductionEnd = request.reduction_end();

        if (!request.reduction_yn()) {
            // 감면 안하면 날짜는 무조건 null로 정리
            reductionStart = null;
            reductionEnd = null;
        } else {
            // 감면 한다면 날짜 필수 + 범위 체크
            if (reductionStart == null || reductionEnd == null)
                throw new ApiException(ErrorCode.BADREQ400, "reduction_yn=true면 reduction_start/end는 필수입니다.");
            if (reductionStart.isAfter(reductionEnd))
                throw new ApiException(ErrorCode.BADREQ400, "reduction_start <= reduction_end 여야 합니다.");
        }

        TaxCase taxCase = new TaxCase(customer);
        taxCase.applySelection(
                request.claim_from(),
                request.claim_to(),
                reductionStart,
                reductionEnd,
                request.claim_date()
        );

        taxCaseRepository.save(taxCase);
        return taxCase.getCaseId();
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

            // 회사 최소 1개 + 최대 3개(엔티티 ID 제약이 Max(3))
            if (yearCase.companies() == null || yearCase.companies().isEmpty()) {
                throw new ApiException(ErrorCode.BADREQ400, "companies는 1개 이상 필요합니다. case_year=" + yearCase.case_year());
            }
            if (yearCase.companies().size() > 3) {
                throw new ApiException(ErrorCode.BADREQ400, "companies는 최대 3개까지 가능합니다. case_year=" + yearCase.case_year());
            }

            // year 작은사업자 여부: 회사들 중 하나라도 true면 true로 저장 (엔티티가 year에만 있음)
            boolean smallBusinessYn = yearCase.companies().stream().anyMatch(RefundCompany::small_business_yn);

            // draft year 저장 (이미 있으면 400)
            CaseDraftYearId yearId = new CaseDraftYearId(caseId, yearCase.case_year());
            if (caseDraftYearRepository.existsById(yearId)) {
                throw new ApiException(ErrorCode.BADREQ400, "이미 입력된 case_year 입니다: " + yearCase.case_year());
            }

            CaseDraftYear draftYear = new CaseDraftYear(
                    taxCase,
                    yearCase.case_year(),
                    smallBusinessYn,
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
                        company.business_number()
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
