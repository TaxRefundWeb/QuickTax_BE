package com.quicktax.demo.api;

import com.quicktax.demo.common.ApiResponse;
import com.quicktax.demo.dto.RefundPageResponse;
import com.quicktax.demo.dto.refundInput.RefundSaveResponse;
import com.quicktax.demo.dto.RefundYearRequest;
import com.quicktax.demo.dto.refundInput.RefundInputRequest;
import com.quicktax.demo.dto.refundInput.WithholdingUploadRequest;
import com.quicktax.demo.service.refund.RefundSelectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "3. 경정청구(Refund)", description = "기간 선택, 상세 정보 입력, 서류 업로드 API")
public class RefundController {

    private final RefundSelectionService refundSelectionService;

    /**
     * 1. 경정청구 기간 및 감면 정보 입력
     */
    @PostMapping("/refund-selection/{customerId}")
    @Operation(summary = "경정청구 기간 및 감면 정보 입력", description = "청구 기간(시작/종료), 신청일, 감면 여부 등을 입력받아 대상 연도를 자동 계산하고, 다음 단계 구성을 위한 데이터를 반환합니다.")
    public ApiResponse<RefundPageResponse> selectRefundYears(
            @AuthenticationPrincipal Long cpaId,
            @Parameter(description = "대상 고객 ID", required = true) @PathVariable Long customerId,
            @RequestBody RefundYearRequest request) {

        return ApiResponse.ok(refundSelectionService.configureRefundPages(cpaId, customerId, request));
    }

    /**
     * 2. 상세 정보 입력 (수정됨: 반환타입 RefundSaveResponse)
     */
    @PostMapping("/refund-claims/{caseId}")
    @Operation(summary = "상세 정보 입력", description = "연도별 근무지, 배우자, 자녀 정보를 입력받아 검증 후 저장하고, 저장된 연도 리스트를 반환합니다.")
    public ApiResponse<RefundSaveResponse> inputRefundInfo(
            @AuthenticationPrincipal Long cpaId,
            @Parameter(description = "생성된 Case ID", required = true) @PathVariable Long caseId,
            @RequestBody RefundInputRequest request) {

        // 💡 서비스 호출 결과를 그대로 반환 (saved_case_years 포함)
        return ApiResponse.ok(refundSelectionService.saveRefundInfo(cpaId, caseId, request));
    }

    /**
     * 3. 서류 업로드
     */
    @PostMapping(value = "/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "서류 업로드 (PDF)", description = "원천징수영수증 PDF 파일들과 메타데이터(JSON)를 함께 업로드합니다.")
    public ApiResponse<String> uploadDocuments(
            @AuthenticationPrincipal Long cpaId,
            @RequestPart("info") WithholdingUploadRequest request,
            @RequestPart("files") List<MultipartFile> files
    ) {

        refundSelectionService.uploadWithholdingFiles(cpaId, request, files);

        return ApiResponse.ok("총 " + files.size() + "개의 문서가 업로드되었습니다.");
    }
}