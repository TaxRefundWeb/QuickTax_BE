package com.quicktax.demo.api;

import com.quicktax.demo.common.ApiResponse;
import com.quicktax.demo.dto.RefundInputRequest;
import com.quicktax.demo.dto.RefundPageResponse;
import com.quicktax.demo.dto.RefundYearRequest;
import com.quicktax.demo.dto.refundInput.WithholdingUploadRequest; // 💡 DTO import
import com.quicktax.demo.service.refund.RefundSelectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/refund")
@RequiredArgsConstructor
public class RefundController {

    private final RefundSelectionService refundSelectionService;

    /**
     * 1. 경정청구 기간 선택 (페이지 수 계산)
     * POST /api/refund/selection
     */
    @PostMapping("/selection")
    public ApiResponse<RefundPageResponse> selectRefundYears(
            @AuthenticationPrincipal Long cpaId,
            @RequestBody RefundYearRequest request) {

        return ApiResponse.ok(refundSelectionService.configureRefundPages(cpaId, request));
    }

    /**
     * 2. 경정청구 상세 정보 입력 (배우자/자녀 포함)
     * POST /api/refund/info
     */
    @PostMapping("/info")
    public ApiResponse<String> inputRefundInfo(
            @AuthenticationPrincipal Long cpaId,
            @RequestBody RefundInputRequest request) {

        refundSelectionService.saveRefundInfo(cpaId, request);
        return ApiResponse.ok("정보 입력이 완료되었습니다.");
    }

    /**
     * 3. 원천징수영수증 PDF 파일 업로드 (JSON + File)
     * POST /api/refund/receipts/upload
     */
    @PostMapping(value = "/receipts/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<String> uploadWithholdingReceipts(
            @AuthenticationPrincipal Long cpaId,
            @RequestPart("info") WithholdingUploadRequest request,  // 📝 JSON 데이터
            @RequestPart("files") List<MultipartFile> files         // 📂 PDF 파일 리스트
    ) {

        refundSelectionService.uploadWithholdingFiles(cpaId, request, files);

        return ApiResponse.ok("총 " + files.size() + "개의 원천징수영수증 파일이 업로드되었습니다.");
    }
}