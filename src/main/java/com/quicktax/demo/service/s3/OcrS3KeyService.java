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
}
