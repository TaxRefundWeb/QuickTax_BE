package com.quicktax.demo.api;

import com.quicktax.demo.common.ApiResponse;
import com.quicktax.demo.dto.RefundResultsResponse;

import com.quicktax.demo.service.result.RefundResultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "5. 결과(계산결과 보기 , 계산결과 선택)", description = ") 계산결과 보기, 계산결과 선택 API")
public class RefundResultController {

    private final RefundResultService refundResultService;

    @GetMapping("/result/{caseId}")
    @Operation(summary = "계산결과 조회", description = "이 case에 모든년도에 해당하는 계산결과를 모두 가져온다.")
    public ApiResponse<RefundResultsResponse> getRefundResults(
            @AuthenticationPrincipal Long cpaId,
            @PathVariable Long caseId
    ) {
        return ApiResponse.ok(refundResultService.getRefundResults(cpaId, caseId));
    }
}
