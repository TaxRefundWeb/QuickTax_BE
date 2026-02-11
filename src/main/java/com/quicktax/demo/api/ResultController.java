package com.quicktax.demo.api;

import com.quicktax.demo.common.ApiResponse;
import com.quicktax.demo.dto.calc.CalcConfirmRequest;
import com.quicktax.demo.dto.calc.CalcDocumentResponse;
import com.quicktax.demo.service.calc.ResultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/result")
@RequiredArgsConstructor
@Tag(name = "5. Result (확정/문서)", description = "계산 확정 및 결과 문서 조회")
public class ResultController {

    private final ResultService resultService;

    @PostMapping("/{caseId}")
    @Operation(summary = "계산식 확정 및 결과 파일 생성 요청", description = "선택한 시나리오로 계산을 확정하고 결과 파일(PDF/ZIP)을 생성합니다.")
    public ApiResponse<String> confirmCalculation(
            @AuthenticationPrincipal Long cpaId,
            @PathVariable Long caseId,
            @Valid @RequestBody CalcConfirmRequest request
    ) {
        resultService.confirmAndGenerateFiles(cpaId, caseId, request);
        return ApiResponse.ok("계산식이 확정되고 결과 파일 생성이 완료되었습니다.");
    }

    @GetMapping("/{caseId}/documents")
    @Operation(summary = "최종 완료 결과(문서 및 환급액) 조회", description = "확정된 계산 결과에 따른 최종 문서 파일과 총 환급액을 조회합니다.")
    public ApiResponse<CalcDocumentResponse> getResultDocuments(
            @AuthenticationPrincipal Long cpaId,
            @PathVariable Long caseId
    ) {
        return ApiResponse.ok(resultService.getResultDocuments(cpaId, caseId));
    }
}
