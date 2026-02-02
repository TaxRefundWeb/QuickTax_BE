package com.quicktax.demo.service.refund;

import com.quicktax.demo.common.ApiException;
import com.quicktax.demo.common.ErrorCode;
import com.quicktax.demo.dto.RefundPageResponse;
import com.quicktax.demo.dto.RefundYearRequest;
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

    public RefundPageResponse configureRefundPages(Long cpaId, RefundYearRequest request) {
        String fromDateStr = request.getClaimFrom();
        String toDateStr = request.getClaimTo();

        // 1. 유효성 검사 (null 체크)
        if (fromDateStr == null || toDateStr == null) {
            throw new ApiException(ErrorCode.BADREQ400, "시작일(claim_from)과 종료일(claim_to)을 모두 입력해주세요.");
        }

        try {
            // 2. 문자열 -> 날짜(LocalDate) 변환
            // ISO_LOCAL_DATE 포맷 (YYYY-MM-DD) 사용
            LocalDate date1 = LocalDate.parse(fromDateStr, DateTimeFormatter.ISO_LOCAL_DATE);
            LocalDate date2 = LocalDate.parse(toDateStr, DateTimeFormatter.ISO_LOCAL_DATE);

            // 3. 연도 추출
            int year1 = date1.getYear();
            int year2 = date2.getYear();

            // 4. 순서 정렬 (작은 연도 ~ 큰 연도)
            int startYear = Math.min(year1, year2);
            int endYear = Math.max(year1, year2);

            // 5. 연도 리스트 생성
            List<Integer> years = new ArrayList<>();
            for (int i = startYear; i <= endYear; i++) {
                years.add(i);
            }

            int count = years.size();
            if (count > 10) {
                throw new ApiException(ErrorCode.BADREQ400, "최대 10년치까지만 한 번에 신청 가능합니다.");
            }

            // 메시지 생성 (예: "2021년부터 2025년까지...")
            String message = String.format("%d년부터 %d년까지 총 %d개 연도에 대한 자료를 입력합니다.", startYear, endYear, count);

            return new RefundPageResponse(count, years, message);

        } catch (DateTimeParseException e) {
            // 날짜 형식이 YYYY-MM-DD가 아닐 경우 예외 처리
            throw new ApiException(ErrorCode.BADREQ400, "날짜 형식은 YYYY-MM-DD여야 합니다. (예: 2025-01-01)");
        }
    }
}