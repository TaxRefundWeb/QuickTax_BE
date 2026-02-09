package com.quicktax.demo.service.refund;

import com.quicktax.demo.common.ApiException;
import com.quicktax.demo.common.ErrorCode;
import com.quicktax.demo.domain.customer.Customer;
import com.quicktax.demo.domain.refund.RefundCase;
import com.quicktax.demo.dto.RefundPageResponse;
import com.quicktax.demo.dto.RefundYearRequest;
import com.quicktax.demo.dto.refundInput.RefundInputRequest;
import com.quicktax.demo.dto.refundInput.RefundInputRequest.RefundYearlyData;
import com.quicktax.demo.dto.refundInput.RefundSaveResponse;
import com.quicktax.demo.dto.refundInput.WithholdingUploadRequest;
import com.quicktax.demo.repo.CustomerRepository;
import com.quicktax.demo.repo.RefundCaseRepository;
import com.quicktax.demo.repo.TaxCompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.quicktax.demo.service.calc.RefundCalculationService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RefundSelectionService {

    private final RefundCaseRepository refundCaseRepository;
    private final TaxCompanyRepository taxCompanyRepository;
    private final CustomerRepository customerRepository;
    private final RefundCalculationService refundCalculationService;

    // 1. 기간 선택 및 Case 생성 (이전과 동일)
    @Transactional
    public RefundPageResponse configureRefundPages(Long cpaId, Long customerId, RefundYearRequest request) {

        if (request.getClaimFrom() == null || request.getClaimTo() == null) {
            throw new ApiException(ErrorCode.BADREQ400, "시작일(claim_from)과 종료일(claim_to)을 모두 입력해주세요.");
        }
        if (request.getClaimDate() == null) {
            throw new ApiException(ErrorCode.BADREQ400, "신청일(claim_date)을 입력해주세요.");
        }
        if (request.getReductionYn() == null || request.getReductionYn().isBlank()) {
            throw new ApiException(ErrorCode.BADREQ400, "감면 여부(reduction_yn)를 선택해주세요.");
        }

        try {
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new ApiException(ErrorCode.COMMON404, "존재하지 않는 고객입니다."));

            if (!customer.getTaxCompany().getCpaId().equals(cpaId)) {
                throw new ApiException(ErrorCode.AUTH403, "해당 고객에 대한 접근 권한이 없습니다.");
            }

            LocalDate fromDate = LocalDate.parse(request.getClaimFrom(), DateTimeFormatter.ISO_LOCAL_DATE);
            LocalDate toDate = LocalDate.parse(request.getClaimTo(), DateTimeFormatter.ISO_LOCAL_DATE);
            LocalDate caseDate = LocalDate.parse(request.getClaimDate(), DateTimeFormatter.ISO_LOCAL_DATE);

            int startYear = Math.min(fromDate.getYear(), toDate.getYear());
            int endYear = Math.max(fromDate.getYear(), toDate.getYear());
            int yearCount = endYear - startYear + 1;

            if (yearCount > 10) {
                throw new ApiException(ErrorCode.BADREQ400, "최대 10년치까지만 한 번에 신청 가능합니다.");
            }

            String reductionStart = request.getReductionStart();
            String reductionEnd = request.getReductionEnd();

            if (!"yes".equalsIgnoreCase(request.getReductionYn())) {
                reductionStart = null;
                reductionEnd = null;
            }

            RefundCase refundCase = RefundCase.builder()
                    .taxCompany(customer.getTaxCompany())
                    .customer(customer)
                    .caseDate(caseDate)
                    .claimStart(request.getClaimFrom())
                    .claimEnd(request.getClaimTo())
                    .reductionYn(request.getReductionYn())
                    .reductionStart(reductionStart)
                    .reductionEnd(reductionEnd)
                    .status("CREATED")
                    .build();

            RefundCase savedCase = refundCaseRepository.save(refundCase);

            return new RefundPageResponse(savedCase.getCaseId());

        } catch (DateTimeParseException e) {
            throw new ApiException(ErrorCode.BADREQ400, "날짜 형식 오류 (YYYY-MM-DD)");
        }
    }

    // 2. 상세 정보 저장 로직 (💡 반환타입 변경: void -> RefundSaveResponse)
    @Transactional
    public RefundSaveResponse saveRefundInfo(Long cpaId, Long caseId, RefundInputRequest request) {

        // 1. Case 조회
        RefundCase refundCase = refundCaseRepository.findById(caseId)
                .orElseThrow(() -> new ApiException(ErrorCode.BADREQ400, "존재하지 않는 Case ID입니다."));

        // 2. 권한 검증 (403)
        Customer customer = refundCase.getCustomer();
        if (customer == null || !customer.getTaxCompany().getCpaId().equals(cpaId)) {
            throw new ApiException(ErrorCode.AUTH403, "해당 경정청구 건에 접근할 권한이 없습니다.");
        }

        List<RefundYearlyData> cases = request.getCases();
        if (cases == null || cases.isEmpty()) {
            throw new ApiException(ErrorCode.BADREQ400, "입력된 연도별 정보(cases)가 없습니다.");
        }

        // 3. 데이터 검증 및 연도 수집
        Set<Integer> yearCheckSet = new HashSet<>();
        List<Integer> savedYears = new ArrayList<>(); // 💡 저장된 연도 수집용 리스트

        for (RefundYearlyData data : cases) {
            // (1) case_year 중복 및 누락 체크
            if (data.getCaseYear() == null) {
                throw new ApiException(ErrorCode.BADREQ400, "연도(case_year) 정보가 누락되었습니다.");
            }
            if (!yearCheckSet.add(data.getCaseYear())) {
                throw new ApiException(ErrorCode.BADREQ400, "중복된 연도(case_year)가 존재합니다: " + data.getCaseYear());
            }

            // (2) companies 필수 체크
            if (data.getCompanies() == null || data.getCompanies().isEmpty()) {
                throw new ApiException(ErrorCode.BADREQ400, data.getCaseYear() + "년도의 근무지(companies) 정보는 최소 1개 이상이어야 합니다.");
            }

            // (3) spouse_yn 체크
            if (Boolean.TRUE.equals(data.getSpouseYn())) {
                if (data.getSpouse() == null) {
                    throw new ApiException(ErrorCode.BADREQ400, data.getCaseYear() + "년도: 배우자가 있다고 체크되었으나 정보가 없습니다.");
                }
            } else {
                if (data.getSpouse() != null) {
                    throw new ApiException(ErrorCode.BADREQ400, data.getCaseYear() + "년도: 배우자가 없다고 체크되었으나 정보가 포함되어 있습니다.");
                }
            }

            // (4) child_yn 체크
            if (Boolean.TRUE.equals(data.getChildYn())) {
                if (data.getChildren() == null || data.getChildren().isEmpty()) {
                    throw new ApiException(ErrorCode.BADREQ400, data.getCaseYear() + "년도: 자녀가 있다고 체크되었으나 정보가 없습니다.");
                }
            } else {
                if (data.getChildren() != null && !data.getChildren().isEmpty()) {
                    throw new ApiException(ErrorCode.BADREQ400, data.getCaseYear() + "년도: 자녀가 없다고 체크되었으나 정보가 포함되어 있습니다.");
                }
            }

            // 검증 통과한 연도 추가
            savedYears.add(data.getCaseYear());
        }

        // 4. 실제 저장 (TODO 구현 필요)
        // refundDetailRepository.saveAll(...) 등
        System.out.println("모든 데이터 검증 통과. Case ID: " + caseId + " 저장 시작...");

        refundCalculationService.calculateRefund(caseId);

        // 💡 5. 결과 반환 (저장된 연도 리스트)
        return new RefundSaveResponse(savedYears);
    }

    // 3. 파일 업로드 처리 로직 (이전과 동일)
    @Transactional
    public void uploadWithholdingFiles(Long cpaId, WithholdingUploadRequest request, List<MultipartFile> files) {

        if (request.getCaseId() == null) {
            throw new ApiException(ErrorCode.BADREQ400, "case_id가 누락되었습니다.");
        }
        if (request.getClaimFrom() == null || request.getClaimTo() == null) {
            throw new ApiException(ErrorCode.BADREQ400, "청구 기간(claim_from, claim_to)을 모두 입력해주세요.");
        }

        if (files == null || files.isEmpty()) {
            throw new ApiException(ErrorCode.BADREQ400, "업로드할 파일이 없습니다.");
        }

        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;

            String contentType = file.getContentType();
            String originalFilename = file.getOriginalFilename();

            if (contentType != null && !contentType.equals("application/pdf")) {
                throw new ApiException(ErrorCode.BADREQ400, "PDF 파일만 업로드 가능합니다: " + originalFilename);
            }
            // TODO: 실제 파일 저장 로직
            System.out.println("파일 수신 성공: " + originalFilename + " (크기: " + file.getSize() + " bytes)");
        }
        System.out.println("업로드 메타정보: CaseID=" + request.getCaseId());
    }
}