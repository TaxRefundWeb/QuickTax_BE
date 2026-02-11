package com.quicktax.demo.ocr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;

@Slf4j
@Component
public class ClovaTemplateOcrClient {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate;

    @Value("${NAVER_OCR_INVOKE_URL:}")
    private String invokeUrl;

    @Value("${NAVER_OCR_SECRET:}")
    private String secret;

    /**
     * 템플릿 ID를 콤마로 나열.
     * - 인덱스 0: salary 템플릿
     * - 인덱스 1: detail 템플릿
     */
    @Value("${NAVER_TEMPLATE_IDS:}")
    private String templateIdsRaw;

    public ClovaTemplateOcrClient() {
        SimpleClientHttpRequestFactory rf = new SimpleClientHttpRequestFactory();
        rf.setConnectTimeout(5_000);
        rf.setReadTimeout(60_000);
        this.restTemplate = new RestTemplate(rf);
    }

    /**
     * [메인] Pipeline에서 쓰는 시그니처.
     * templateIndex:
     * - 0이면 templateIds[0] 사용
     * - 1이면 templateIds[1] 사용
     * - 없으면 templateIds를 아예 안 넣어서 “자동 분류”로 보낸다.
     */
    public JsonNode inferPdf(Path pdfFile, String nameForResponse, int templateIndex) {
        requireConfig();

        Integer templateId = pickTemplateId(templateIndex);
        return inferPdfInternal(pdfFile, nameForResponse, templateId);
    }

    /**
     * [호환용] 네가 어딘가에서 inferPdf(byte[], int, String)로 만들어둔 흔적 때문에 추가.
     * - 여기의 int는 "templateId"로 취급한다.
     * - Pipeline은 이 오버로드 안 씀.
     */
    public JsonNode inferPdf(byte[] pdfBytes, int templateId, String nameForResponse) {
        requireConfig();

        Path tmp = null;
        try {
            tmp = Files.createTempFile("quicktax_ocr_", ".pdf");
            Files.write(tmp, pdfBytes);
            return inferPdfInternal(tmp, nameForResponse, templateId);
        } catch (Exception e) {
            throw new RuntimeException("OCR call failed (byte[] overload): " + e.getMessage(), e);
        } finally {
            if (tmp != null) {
                try { Files.deleteIfExists(tmp); } catch (Exception ignore) {}
            }
        }
    }

    private JsonNode inferPdfInternal(Path pdfFile, String nameForResponse, Integer templateId) {
        int[] backoffs = new int[]{0, 500, 1500};

        for (int attempt = 0; attempt < backoffs.length; attempt++) {
            try {
                if (backoffs[attempt] > 0) Thread.sleep(backoffs[attempt]);

                String messageJson = buildMessageJson(nameForResponse, templateId);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.MULTIPART_FORM_DATA);
                headers.setAccept(List.of(MediaType.APPLICATION_JSON));
                headers.set("X-OCR-SECRET", secret);

                MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
                body.add("file", new FileSystemResource(pdfFile));

                HttpHeaders msgHeaders = new HttpHeaders();
                msgHeaders.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<String> msgEntity = new HttpEntity<>(messageJson, msgHeaders);
                body.add("message", msgEntity);

                HttpEntity<MultiValueMap<String, Object>> req = new HttpEntity<>(body, headers);

                ResponseEntity<String> res = restTemplate.exchange(
                        invokeUrl,
                        HttpMethod.POST,
                        req,
                        String.class
                );

                if (!res.getStatusCode().is2xxSuccessful()) {
                    throw new RuntimeException("OCR HTTP " + res.getStatusCode() + ": " + safeBody(res.getBody()));
                }

                String respBody = res.getBody();
                if (respBody == null || respBody.isBlank()) {
                    throw new RuntimeException("OCR response body is empty");
                }

                return objectMapper.readTree(respBody);

            } catch (HttpClientErrorException e) {
                throw new RuntimeException("OCR 4xx: " + e.getStatusCode() + " " + safeBody(e.getResponseBodyAsString()), e);
            } catch (HttpServerErrorException | ResourceAccessException e) {
                if (attempt == backoffs.length - 1) {
                    throw new RuntimeException("OCR 5xx/timeout after retries: " + e.getMessage(), e);
                }
                log.warn("OCR retryable error attempt={}/{} msg={}", attempt + 1, backoffs.length, e.getMessage());
            } catch (Exception e) {
                throw new RuntimeException("OCR call/parse failed: " + e.getMessage(), e);
            }
        }

        throw new RuntimeException("OCR retry loop ended unexpectedly");
    }

    private void requireConfig() {
        if (invokeUrl == null || invokeUrl.isBlank()) throw new IllegalStateException("NAVER_OCR_INVOKE_URL is blank");
        if (secret == null || secret.isBlank()) throw new IllegalStateException("NAVER_OCR_SECRET is blank");
    }

    private Integer pickTemplateId(int templateIndex) {
        List<Integer> ids = parseTemplateIds(templateIdsRaw);
        if (templateIndex < 0 || templateIndex >= ids.size()) return null; // 자동 분류
        return ids.get(templateIndex);
    }

    private String buildMessageJson(String name, Integer templateId) throws Exception {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("version", "V2");
        root.put("requestId", UUID.randomUUID().toString());
        root.put("timestamp", Instant.now().toEpochMilli());
        root.put("lang", "ko");

        Map<String, Object> image = new LinkedHashMap<>();
        image.put("format", "pdf");
        image.put("name", name);

        if (templateId != null) {
            image.put("templateIds", List.of(templateId));
        }

        root.put("images", List.of(image));
        return objectMapper.writeValueAsString(root);
    }

    private static List<Integer> parseTemplateIds(String raw) {
        if (raw == null || raw.isBlank()) return List.of();
        String[] parts = raw.split(",");
        List<Integer> out = new ArrayList<>();
        for (String p : parts) {
            String s = p.trim();
            if (s.isBlank()) continue;
            try { out.add(Integer.parseInt(s)); } catch (NumberFormatException ignore) {}
        }
        return out;
    }

    private static String safeBody(String body) {
        if (body == null) return "";
        return body.length() > 500 ? body.substring(0, 500) + "..." : body;
    }
}
