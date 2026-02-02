package com.quicktax.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class RefundPageResponse {
    private int totalPageCount;        // 생성해야 할 페이지 수
    private List<Integer> validYears;  // 검증된 연도 리스트
    private String message;            // 안내 메시지 (예: "3개 연도에 대한 자료를 입력해주세요.")
}