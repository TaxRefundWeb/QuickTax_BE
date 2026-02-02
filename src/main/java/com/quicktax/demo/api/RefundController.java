package com.quicktax.demo.api;

import com.quicktax.demo.common.ApiResponse;
import com.quicktax.demo.dto.RefundInputRequest;
import com.quicktax.demo.dto.RefundPageResponse;
import com.quicktax.demo.dto.RefundYearRequest;
import com.quicktax.demo.dto.refundInput.WithholdingUploadRequest;
import com.quicktax.demo.service.refund.RefundSelectionService;
import io.swagger.v3.oas.annotations.Operation; // 💡 import 추가
import io.swagger.v3.oas.annotations.tags.Tag; // 💡 import 추가
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

    @PostMapping("/refund-selection")
    @Operation(summary = "경정청구 기간 선택", description = "시작일과 종료일을 입력하면 청구 가능한 연도 리스트와 페이지 수를 반환합니다.")
    public ApiResponse<RefundPageResponse> selectRefundYears(
            @AuthenticationPrincipal Long cpaId,
            @RequestBody RefundYearRequest request) {

        return ApiResponse.ok(refundSelectionService.configureRefundPages(cpaId, request));
    }

    @PostMapping("/refund-claims")
    @Operation(summary = "상세 정보 입력", description = "법인명, 근무기간, 가족관계(배우자/자녀) 등 상세 정보를 저장합니다.")
    public ApiResponse<String> inputRefundInfo(
            @AuthenticationPrincipal Long cpaId,
            @RequestBody RefundInputRequest request) {

        refundSelectionService.saveRefundInfo(cpaId, request);
        return ApiResponse.ok("정보 입력이 완료되었습니다.");
    }

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