package com.quicktax.demo.api;

import com.quicktax.demo.common.ApiResponse;
import com.quicktax.demo.config.UserDetailsImpl;
import com.quicktax.demo.dto.calc.CalcConfirmRequest;
import com.quicktax.demo.dto.calc.CalcDocumentResponse;
import com.quicktax.demo.dto.refund.RefundResultsResponse;
import com.quicktax.demo.service.calc.ResultService;
import com.quicktax.demo.service.result.RefundResultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/result")
@RequiredArgsConstructor
@Tag(name = "5. Result (계산 결과 조회, 확정, 문서 출력)")
public class ResultController {

    private final ResultService resultService;
    private final RefundResultService refundResultService;

    // 1. 계산 결과 조회 (기존 RefundResultController 기능)
    // GET /api/result/{caseId}
    @Operation(summary = "계산 결과 조회 (시나리오별 환급액 확인)", description = "해당 Case의 모든 연도/시나리오별 계산 결과를 조회합니다.")
    @GetMapping("/{caseId}")
    public ApiResponse<RefundResultsResponse> getRefundResults(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long caseId
    ) {
        // RefundResultService를 호출하여 계산 결과(DTO)를 반환
        return ApiResponse.ok(refundResultService.getRefundResults(userDetails.getCpaId(), caseId));
    }

    // 2. 계산식 확정 및 결과 파일 생성 (기존 ResultController 기능)
    // POST /api/result/{caseId}
    @Operation(summary = "계산식 확정 및 결과 파일 생성 요청", description = "선택한 시나리오로 계산을 확정하고 결과 파일(PDF/ZIP)을 생성합니다.")
    @PostMapping("/{caseId}")
    public ApiResponse<String> confirmCalculation(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long caseId,
            @Valid @RequestBody CalcConfirmRequest request
    ) {
        // ResultService를 호출하여 저장 및 파일 생성
        resultService.confirmAndGenerateFiles(userDetails.getCpaId(), caseId, request);
        return ApiResponse.ok("계산식이 확정되고 결과 파일 생성이 완료되었습니다.");
    }

    // 3. 최종 완료 결과 문서 조회 (기존 ResultController 기능)
    // GET /api/result/{caseId}/documents
    @Operation(summary = "최종 완료 결과(문서 및 환급액) 조회", description = "확정된 계산 결과에 따른 최종 문서 파일과 총 환급액을 조회합니다.")
    @GetMapping("/{caseId}/documents")
    public ApiResponse<CalcDocumentResponse> getResultDocuments(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long caseId
    ) {
        // ResultService를 호출하여 최종 문서 정보 조회
        CalcDocumentResponse response = resultService.getResultDocuments(userDetails.getCpaId(), caseId);
        return ApiResponse.ok(response);
    }
}