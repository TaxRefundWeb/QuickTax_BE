package com.quicktax.demo.service.calc;

import com.quicktax.demo.common.ApiException;
import com.quicktax.demo.common.ErrorCode;
import com.quicktax.demo.domain.calc.*;
import com.quicktax.demo.domain.cases.TaxCase;
import com.quicktax.demo.dto.calc.CalcConfirmRequest;
import com.quicktax.demo.dto.calc.CalcConfirmRequest.YearScenario;
import com.quicktax.demo.repo.TaxCaseRepository;
import com.quicktax.demo.repo.calc.CaseCalcResultDocumentAllRepository;
import com.quicktax.demo.repo.calc.CaseCalcResultDocumentRepository;
import com.quicktax.demo.repo.calc.CaseCalcResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResultService {

    private final TaxCaseRepository taxCaseRepository;
    private final CaseCalcResultRepository resultRepository;
    private final CaseCalcResultDocumentRepository documentRepository;
    private final CaseCalcResultDocumentAllRepository documentAllRepository;

    // 유효한 시나리오 목록 (검증용)
    private static final List<String> ALLOWED_SCENARIOS = Arrays.asList(
            "청년 경정청구 신청", "자녀 경정청구 신청", "청년+자녀 경정청구 신청",
            "청년 완료 이후 자녀 추가 경정청구 신청", "자녀 완료 이후 청년 추가 경정청구 신청",
            "이중근로 경정청구 신청", "기한 이후 경정청구 신청"
    );

    // (가짜) 파일 생성 로직
    private String generateFileUrl(Long caseId, Integer year, String type) {
        return "https://s3.quicktax.com/files/" + caseId + "/" + year + "_" + type + ".pdf";
    }

    private String generateTotalFileUrl(Long caseId) {
        return "https://s3.quicktax.com/files/" + caseId + "/total_result.zip";
    }

    @Transactional
    // 💡 [수정] 권한 검사를 위해 cpaId 파라미터 추가
    public void confirmAndGenerateFiles(Long cpaId, Long caseId, CalcConfirmRequest request) {

        // 1. Case 조회
        TaxCase taxCase = taxCaseRepository.findById(caseId)
                .orElseThrow(() -> new ApiException(ErrorCode.COMMON404, "존재하지 않는 Case입니다."));

        // 2. [추가] 권한 검증 (AUTH403)
        Long ownerCpaId = taxCase.getCustomer().getTaxCompany().getCpaId();
        if (!cpaId.equals(ownerCpaId)) {
            throw new ApiException(ErrorCode.AUTH403, "권한이 존재하지 않습니다.");
        }

        long totalRefundAmount = 0L;

        // 3. 요청된 연도별 시나리오 처리
        for (YearScenario scenario : request.getScenarios()) {
            Integer year = scenario.getCaseYear();
            String code = scenario.getScenarioCode();

            // 3-1. 시나리오 유효성 검증
            // 💡 [수정] COMMON400 -> COMMON404 ("계산 방식이 존재하지 않습니다" 의미 활용)
            if (!ALLOWED_SCENARIOS.contains(code)) {
                throw new ApiException(ErrorCode.COMMON404, "존재하지 않는 계산 방식(시나리오)입니다: " + code);
            }

            // 3-2. 해당 시나리오의 계산 결과(환급액) 조회
            CaseCalcResultId resultId = new CaseCalcResultId(caseId, year, code);

            CaseCalcResult calcResult = resultRepository.findById(resultId)
                    .orElseThrow(() -> new ApiException(ErrorCode.COMMON404,
                            String.format("%d년도 [%s]에 대한 계산 데이터가 없습니다.", year, code)));

            // 3-3. 환급액 합산
            if (calcResult.getRefundAmount() != null) {
                totalRefundAmount += calcResult.getRefundAmount();
            }

            // 3-4. 연도별 파일 생성 및 저장
            String fileUrl = generateFileUrl(caseId, year, "report");

            CaseCalcResultDocument document = new CaseCalcResultDocument(taxCase, year, fileUrl);
            documentRepository.save(document);
        }

        // 4. 전체 통합 결과 저장
        String totalUrl = generateTotalFileUrl(caseId);

        CaseCalcResultDocumentAll documentAll = new CaseCalcResultDocumentAll(taxCase, totalUrl, totalRefundAmount);
        documentAllRepository.save(documentAll);

        log.info("CaseId: {} 결과 확정 완료. 총 환급액: {}", caseId, totalRefundAmount);
    }
}