package com.quicktax.demo.dto.calc;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.quicktax.demo.domain.calc.CaseCalcResultDocument;
import com.quicktax.demo.domain.calc.CaseCalcResultDocumentAll;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CalcDocumentResponse {

    // 1. 전체 결과 (환급액 합계 + 통합 파일)
    @JsonProperty("total_result")
    private TotalResult totalResult;

    // 2. 연도별 결과 파일 리스트
    @JsonProperty("year_documents")
    private List<YearDocument> yearDocuments;

    @Getter
    @Builder
    public static class TotalResult {
        @JsonProperty("total_refund_amount")
        private Long totalRefundAmount;

        @JsonProperty("total_file_url")
        private String totalFileUrl;

        // Entity -> DTO 변환 메서드
        public static TotalResult from(CaseCalcResultDocumentAll entity) {
            return TotalResult.builder()
                    .totalRefundAmount(entity.getTotalRefundAmount())
                    .totalFileUrl(entity.getTotalUrl())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class YearDocument {
        @JsonProperty("case_year")
        private Integer caseYear;

        @JsonProperty("file_url")
        private String fileUrl;

        // Entity -> DTO 변환 메서드
        public static YearDocument from(CaseCalcResultDocument entity) {
            return YearDocument.builder()
                    .caseYear(entity.getId().getCaseYear()) // 복합키에서 연도 추출
                    .fileUrl(entity.getUrl())
                    .build();
        }
    }
}