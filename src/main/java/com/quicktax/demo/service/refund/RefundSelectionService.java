package com.quicktax.demo.service.refund;

import com.quicktax.demo.common.ApiException;
import com.quicktax.demo.common.ErrorCode;
import com.quicktax.demo.dto.*;
import com.quicktax.demo.dto.ChildInfo;
import com.quicktax.demo.dto.RefundInputRequest;
import com.quicktax.demo.dto.refundInput.RefundDetailInfo;
import com.quicktax.demo.dto.refundInput.WithholdingUploadRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RefundSelectionService {

    // 1. 기간 선택 및 페이지 계산 로직
    public RefundPageResponse configureRefundPages(Long cpaId, RefundYearRequest request) {
        String fromDateStr = request.getClaimFrom();
        String toDateStr = request.getClaimTo();

        if (fromDateStr == null || toDateStr == null) {
            throw new ApiException(ErrorCode.BADREQ400, "시작일(claim_from)과 종료일(claim_to)을 모두 입력해주세요.");
        }

        try {
            LocalDate date1 = LocalDate.parse(fromDateStr, DateTimeFormatter.ISO_LOCAL_DATE);
            LocalDate date2 = LocalDate.parse(toDateStr, DateTimeFormatter.ISO_LOCAL_DATE);

            int startYear = Math.min(date1.getYear(), date2.getYear());
            int endYear = Math.max(date1.getYear(), date2.getYear());

            List<Integer> years = new ArrayList<>();
            for (int i = startYear; i <= endYear; i++) {
                years.add(i);
            }

            if (years.size() > 10) {
                throw new ApiException(ErrorCode.BADREQ400, "최대 10년치까지만 한 번에 신청 가능합니다.");
            }

            String message = String.format("%d년부터 %d년까지 총 %d개 연도에 대한 자료를 입력합니다.", startYear, endYear, years.size());
            return new RefundPageResponse(years.size(), years, message);

        } catch (DateTimeParseException e) {
            throw new ApiException(ErrorCode.BADREQ400, "날짜 형식 오류 (YYYY-MM-DD)");
        }
    }

    // 2. 상세 정보 저장 로직 (플랫 구조)
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
            // TODO: DB 저장 로직
            System.out.println("상세 정보 저장 완료 - 사업자번호: " + info.getBusinessNumber());
        }
    }

    // 💡 3. 파일 업로드 처리 로직 (신규 추가)
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
            // (주의: 일부 브라우저나 도구는 타입을 안 보낼 수도 있으므로 null 체크 필요)
            if (contentType != null && !contentType.equals("application/pdf")) {
                throw new ApiException(ErrorCode.BADREQ400, "PDF 파일만 업로드 가능합니다: " + originalFilename);
            }

            // TODO: 실제 파일 저장 로직 (S3 업로드 or 로컬 디스크 저장)
            // 예: s3Service.upload(file, "receipts/" + request.getCaseId());

            System.out.println("파일 수신 성공: " + originalFilename + " (크기: " + file.getSize() + " bytes)");
        }

        System.out.println("업로드 메타정보: CaseID=" + request.getCaseId() + ", 기간=" + request.getClaimFrom() + "~" + request.getClaimTo());
    }
}