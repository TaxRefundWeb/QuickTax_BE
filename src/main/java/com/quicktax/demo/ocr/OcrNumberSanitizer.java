package com.quicktax.demo.ocr;

public class OcrNumberSanitizer {

    private OcrNumberSanitizer() {}

    public static Long toLongOrNull(String raw) {
        if (raw == null) return null;
        String s = raw.trim();
        if (s.isEmpty()) return null;

        String cleaned = s.replaceAll("[^0-9\\-]", "");
        if (cleaned.isBlank() || cleaned.equals("-")) return null;

        try {
            return Long.parseLong(cleaned);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static long toLongOrZero(String raw) {
        Long v = toLongOrNull(raw);
        return v == null ? 0L : v;
    }
}
