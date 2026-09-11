package com.finance.ai.guardrail;

import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class OutputGuardService {

    // Examples — tune to your real PII shapes (account number format, card format, etc.)
    private static final Pattern ACCOUNT_NUMBER = Pattern.compile("\\b\\d{9,18}\\b");
    private static final Pattern CARD_NUMBER = Pattern.compile("\\b(?:\\d[ -]*?){13,16}\\b");
    private static final Pattern SSN_LIKE = Pattern.compile("\\b\\d{3}-\\d{2}-\\d{4}\\b");

    public GuardResult screen(String reply) {
        if (reply == null) return GuardResult.clean();
        if (CARD_NUMBER.matcher(reply).find() || SSN_LIKE.matcher(reply).find()) {
            return GuardResult.flagged("Potential sensitive identifier in output.");
        }
        return GuardResult.clean();
    }

    public String sanitize(String reply) {
        String sanitized = CARD_NUMBER.matcher(reply).replaceAll("[redacted]");
        sanitized = SSN_LIKE.matcher(sanitized).replaceAll("[redacted]");
        return sanitized;
    }

    public record GuardResult(boolean flagged, String reason) {
        static GuardResult clean() { return new GuardResult(false, null); }
        static GuardResult flagged(String reason) { return new GuardResult(true, reason); }
    }
}