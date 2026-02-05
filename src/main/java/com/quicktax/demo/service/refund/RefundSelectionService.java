package com.quicktax.demo.service.refund;

import com.quicktax.demo.common.ApiException;
import com.quicktax.demo.common.ErrorCode;
import com.quicktax.demo.domain.auth.TaxCompany;
import com.quicktax.demo.domain.customer.Customer;
import com.quicktax.demo.domain.refund.RefundCase;
import com.quicktax.demo.dto.*;
import com.quicktax.demo.dto.ChildInfo;
import com.quicktax.demo.dto.RefundInputRequest;
import com.quicktax.demo.dto.refundInput.RefundDetailInfo;
import com.quicktax.demo.dto.refundInput.WithholdingUploadRequest;
import com.quicktax.demo.repo.CustomerRepository;
import com.quicktax.demo.repo.RefundCaseRepository;
import com.quicktax.demo.repo.TaxCompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RefundSelectionService {

    private final RefundCaseRepository refundCaseRepository;
    private final TaxCompanyRepository taxCompanyRepository;
    private final CustomerRepository customerRepository;

    // 1. 기간 선택 및 Case 생성 (Result에 caseId만 반환)
    @Transactional
    public RefundPageResponse configureRefundPages(Long cpaId, Long customerId, RefundYearRequest request) { // 💡 customerId 추가

        // 1-1. 필수 값 검증
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
            // 1-2. 고객 조회 및 권한 검증 (💡 추가된 로직)
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new ApiException(ErrorCode.COMMON404, "존재하지 않는 고객입니다."));

            if (!customer.getTaxCompany().getCpaId().equals(cpaId)) {
                throw new ApiException(ErrorCode.AUTH403, "해당 고객에 대한 접근 권한이 없습니다.");
            }

            // 1-3. 날짜 파싱 및 기간 검증
            LocalDate fromDate = LocalDate.parse(request.getClaimFrom(), DateTimeFormatter.ISO_LOCAL_DATE);
            LocalDate toDate = LocalDate.parse(request.getClaimTo(), DateTimeFormatter.ISO_LOCAL_DATE);
            LocalDate caseDate = LocalDate.parse(request.getClaimDate(), DateTimeFormatter.ISO_LOCAL_DATE);

            int startYear = Math.min(fromDate.getYear(), toDate.getYear());
            int endYear = Math.max(fromDate.getYear(), toDate.getYear());

            int yearCount = endYear - startYear + 1;

            if (yearCount > 10) {
                throw new ApiException(ErrorCode.BADREQ400, "최대 10년치까지만 한 번에 신청 가능합니다.");
            }

            // 1-4. 감면 기한 데이터 정리
            String reductionStart = request.getReductionStart();
            String reductionEnd = request.getReductionEnd();

            if (!"yes".equalsIgnoreCase(request.getReductionYn())) {
                reductionStart = null;
                reductionEnd = null;
            }

            // 1-5. DB 저장 (RefundCase 엔티티 생성)
            // 고객 정보(customer)와 세무법인(TaxCompany) 정보 모두 연결
            RefundCase refundCase = RefundCase.builder()
                    .taxCompany(customer.getTaxCompany()) // 고객 정보에서 TaxCompany 가져옴 (일관성 유지)
                    .customer(customer) // 💡 조회한 customer 객체 사용
                    .caseDate(caseDate)
                    .claimStart(request.getClaimFrom())
                    .claimEnd(request.getClaimTo())
                    .reductionYn(request.getReductionYn())
                    .reductionStart(reductionStart)
                    .reductionEnd(reductionEnd)
                    .status("CREATED") // 초기 상태
                    .build();

            // INSERT 실행 및 ID 획득
            RefundCase savedCase = refundCaseRepository.save(refundCase);

            // 1-6. 결과 반환 (오직 caseId만 포함)
            return new RefundPageResponse(savedCase.getCaseId());

        } catch (DateTimeParseException e) {
            throw new ApiException(ErrorCode.BADREQ400, "날짜 형식 오류 (YYYY-MM-DD)");
        }
    }

    // 2. 상세 정보 저장 로직 (기존 유지)
    @Transactional
    public void saveRefundInfo(Long cpaId, RefundInputRequest request) {
        if (request.getCustomerId() == null) {
            throw new ApiException(ErrorCode.BADREQ400, "고객 ID(customerid)가 누락되었습니다.");
        }

        List<RefundDetailInfo> customers = request.getCustomers();
        if (customers == null || customers.isEmpty()) {
            throw new ApiException(ErrorCode.BADREQ400, "입력된 상세 정보가 없습니다.");
        }

        for (RefundDetailInfo info : customers) {
            // 배우자 검증
            if ("yes".equalsIgnoreCase(info.getSpouseYn())) {
                if (info.getSpouseName() == null || info.getSpouseName().isBlank() ||
                        info.getSpouseRrn() == null || info.getSpouseRrn().isBlank()) {
                    throw new ApiException(ErrorCode.BADREQ400, "배우자의 이름과 주민번호를 입력해주세요.");
                }
            }
            // 자녀 검증
            if (info.getChildList() != null) {
                for (ChildInfo child : info.getChildList()) {
                    if ("yes".equalsIgnoreCase(child.getChildYn())) {
                        if (child.getChildName() == null || child.getChildName().isBlank() ||
                                child.getChildRrn() == null || child.getChildRrn().isBlank()) {
                            throw new ApiException(ErrorCode.BADREQ400, "자녀의 이름과 주민번호를 모두 입력해주세요.");
                        }
                    }
                }
            }
            // TODO: DB 저장 로직 (Repository 연결 필요)
            System.out.println("상세 정보 저장 완료 - 사업자번호: " + info.getBusinessNumber());
        }
    }

    // 3. 파일 업로드 처리 로직 (기존 유지)
    @Transactional
    public void uploadWithholdingFiles(Long cpaId, WithholdingUploadRequest request, List<MultipartFile> files) {

        // 1. 메타데이터(JSON) 검증
        if (request.getCaseId() == null) {
            throw new ApiException(ErrorCode.BADREQ400, "case_id가 누락되었습니다.");
        }
        if (request.getClaimFrom() == null || request.getClaimTo() == null) {
            throw new ApiException(ErrorCode.BADREQ400, "청구 기간(claim_from, claim_to)을 모두 입력해주세요.");
        }

        // 2. 파일 리스트 검증
        if (files == null || files.isEmpty()) {
            throw new ApiException(ErrorCode.BADREQ400, "업로드할 파일이 없습니다.");
        }

        // 3. 개별 파일 검증 (PDF 체크)
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue;
            }

            String contentType = file.getContentType();
            String originalFilename = file.getOriginalFilename();

            // Content-Type이 application/pdf 인지 확인
            if (contentType != null && !contentType.equals("application/pdf")) {
                throw new ApiException(ErrorCode.BADREQ400, "PDF 파일만 업로드 가능합니다: " + originalFilename);
            }

            // TODO: 실제 파일 저장 로직 (S3 업로드 or 로컬 디스크 저장)
            System.out.println("파일 수신 성공: " + originalFilename + " (크기: " + file.getSize() + " bytes)");
        }

        System.out.println("업로드 메타정보: CaseID=" + request.getCaseId() + ", 기간=" + request.getClaimFrom() + "~" + request.getClaimTo());
    }
}