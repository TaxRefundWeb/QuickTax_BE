package com.quicktax.demo.api;

import com.quicktax.demo.common.ApiResponse;
import com.quicktax.demo.dto.RefundInputRequest;
import com.quicktax.demo.dto.RefundPageResponse;
import com.quicktax.demo.dto.RefundYearRequest;
import com.quicktax.demo.dto.refundInput.WithholdingUploadRequest;
import com.quicktax.demo.service.refund.RefundSelectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
// 💡 기본 경로를 /api/refund -> /api 로 변경 (하위 경로 유연성 확보)
@RequestMapping("/api")
@RequiredArgsConstructor
public class RefundController {

    private final RefundSelectionService refundSelectionService;

    /**
     * 1. 경정청구 기간 선택 (기존: /selection -> 변경: /refund-selection)
     * POST /api/refund-selection
     */
    @PostMapping("/refund-selection")
    public ApiResponse<RefundPageResponse> selectRefundYears(
            @AuthenticationPrincipal Long cpaId,
            @RequestBody RefundYearRequest request) {

        return ApiResponse.ok(refundSelectionService.configureRefundPages(cpaId, request));
    }

    /**
     * 2. 경정청구 상세 정보 입력 (기존: /info -> 변경: /refund-claims)
     * POST /api/refund-claims
     */
    @PostMapping("/refund-claims")
    public ApiResponse<String> inputRefundInfo(
            @AuthenticationPrincipal Long cpaId,
            @RequestBody RefundInputRequest request) {

        refundSelectionService.saveRefundInfo(cpaId, request);
        return ApiResponse.ok("정보 입력이 완료되었습니다.");
    }

    /**
     * 3. 원천징수 PDF 업로드 (기존: /receipts/upload -> 변경: /documents)
     * POST /api/documents
     */
    @PostMapping(value = "/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<String> uploadDocuments(
            @AuthenticationPrincipal Long cpaId,
            @RequestPart("info") WithholdingUploadRequest request,
            @RequestPart("files") List<MultipartFile> files
    ) {

        refundSelectionService.uploadWithholdingFiles(cpaId, request, files);

        return ApiResponse.ok("총 " + files.size() + "개의 문서가 업로드되었습니다.");
    }
}