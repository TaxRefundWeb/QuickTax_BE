package com.quicktax.demo.ocr;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.LinkedHashMap;
import java.util.Map;

public class ClovaTemplateFieldExtractor {

    private ClovaTemplateFieldExtractor() {}

    public static Map<String, String> extractNameToInferText(JsonNode root) {
        Map<String, String> out = new LinkedHashMap<>();
        if (root == null) return out;

        JsonNode images = root.path("images");
        if (!images.isArray() || images.isEmpty()) return out;
        JsonNode first = images.get(0);

        JsonNode fields = first.path("fields");
        if (!fields.isArray()) return out;

        for (JsonNode f : fields) {
            String name = f.path("name").asText(null);
            if (name == null || name.isBlank()) continue;
            String text = f.path("inferText").asText("");
            out.put(name, text);
        }
        return out;
    }
}
