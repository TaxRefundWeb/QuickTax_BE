package com.quicktax.demo.service.refund;

import com.quicktax.demo.common.ApiException;
import com.quicktax.demo.common.ErrorCode;
import com.quicktax.demo.dto.*;
import com.quicktax.demo.dto.ChildInfo;
import com.quicktax.demo.dto.RefundInputRequest;
import com.quicktax.demo.dto.refundInput.RefundDetailInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RefundSelectionService {

    // 1. 기간 선택 로직 (기존 유지)
    public RefundPageResponse configureRefundPages(Long cpaId, RefundYearRequest request) {
        String fromDateStr = request.getClaimFrom();
        String toDateStr = request.getClaimTo();

        if (fromDateStr == null || toDateStr == null) {
            throw new ApiException(ErrorCode.BADREQ400, "시작일과 종료일을 모두 입력해주세요.");
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
                throw new ApiException(ErrorCode.BADREQ400, "최대 10년치까지만 가능합니다.");
            }

            String message = String.format("%d년부터 %d년까지 총 %d개 연도 데이터 입력", startYear, endYear, years.size());
            return new RefundPageResponse(years.size(), years, message);

        } catch (DateTimeParseException e) {
            throw new ApiException(ErrorCode.BADREQ400, "날짜 형식 오류 (YYYY-MM-DD)");
        }
    }

    // 💡 2. 상세 정보 저장 로직 (플랫 구조 대응)
    public void saveRefundInfo(Long cpaId, RefundInputRequest request) {
        // 기본 ID 검증
        if (request.getCustomerId() == null) {
            throw new ApiException(ErrorCode.BADREQ400, "고객 ID(customerid)가 없습니다.");
        }

        List<RefundDetailInfo> customers = request.getCustomers();
        if (customers == null || customers.isEmpty()) {
            throw new ApiException(ErrorCode.BADREQ400, "입력된 상세 정보가 없습니다.");
        }

        // 각 연도(또는 사업장)별 데이터 순회
        for (RefundDetailInfo info : customers) {

            // ✅ 배우자 검증 (Flat 필드 확인)
            if ("yes".equalsIgnoreCase(info.getSpouseYn())) {
                if (info.getSpouseName() == null || info.getSpouseName().isBlank() ||
                        info.getSpouseRrn() == null || info.getSpouseRrn().isBlank()) {
                    throw new ApiException(ErrorCode.BADREQ400, "배우자의 이름과 주민번호를 입력해주세요.");
                }
            }

            // ✅ 자녀 검증 (List 확인)
            if (info.getChildList() != null && !info.getChildList().isEmpty()) {
                for (ChildInfo child : info.getChildList()) {
                    // 자녀가 있다고(yes) 했는데 정보가 비어있는 경우 체크
                    if ("yes".equalsIgnoreCase(child.getChildYn())) {
                        if (child.getChildName() == null || child.getChildName().isBlank() ||
                                child.getChildRrn() == null || child.getChildRrn().isBlank()) {
                            throw new ApiException(ErrorCode.BADREQ400, "자녀의 이름과 주민번호를 모두 입력해주세요.");
                        }
                    }
                }
            }

            // TODO: DB 저장 (Entity 변환 후 repository.save)
            // request.getCustomerId()와 info 내용을 조합하여 저장
            System.out.println("검증 완료 - 사업자번호: " + info.getBusinessNumber());
        }
    }
}