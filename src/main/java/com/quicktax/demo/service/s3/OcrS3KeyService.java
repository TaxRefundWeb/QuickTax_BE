package com.quicktax.demo.service.s3;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.UUID;

@Service
public class OcrS3KeyService {
    private final Environment env;

    public OcrS3KeyService(Environment env) {
        this.env = env;
    }

    private String prefix() {
        return Arrays.stream(env.getActiveProfiles())
                .filter(p -> p.equals("stg") || p.equals("prod"))
                .findFirst()
                .orElse("stg");
    }

    public String rawPdfKey(Long caseId) {
        return prefix() + "/cases/" + caseId + "/inbox/raw/" + UUID.randomUUID() + ".pdf";
    }

    /** 원본 key로부터 env prefix(stg/prod)를 안전하게 추출 */
    public String envPrefixFromKey(String s3Key) {
        if (s3Key == null) return prefix();
        int idx = s3Key.indexOf("/cases/");
        if (idx > 0) {
            return s3Key.substring(0, idx);
        }
        return prefix();
    }

    public String inboxYearPrefix(String envPrefix, Long caseId) {
        return envPrefix + "/cases/" + caseId + "/inbox/year/";
    }

    public String outboxPrefix(String envPrefix, Long caseId) {
        return envPrefix + "/cases/" + caseId + "/outbox/";
    }

    public String inboxYearPdfKey(String envPrefix, Long caseId, int caseYear) {
        return envPrefix + "/cases/" + caseId + "/inbox/year/" + caseYear + ".pdf";
    }

    public String outboxYearOcrJsonKey(String envPrefix, Long caseId, int caseYear) {
        return envPrefix + "/cases/" + caseId + "/outbox/year/" + caseYear + "/ocr.json";
    }
}
