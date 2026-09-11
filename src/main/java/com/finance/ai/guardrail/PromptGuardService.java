package com.finance.ai.guardrail;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class PromptGuardService {

    // Not exhaustive — a real deployment should back this with a classifier,
    // but a pattern-based first line of defense catches the common cases cheaply.
    private static final List<Pattern> SUSPICIOUS_PATTERNS = List.of(
            Pattern.compile("(?i)ignore (all |previous |the )?(instructions|prompt)"),
            Pattern.compile("(?i)you are now"),
            Pattern.compile("(?i)disregard (your|the) (system|previous) prompt"),
            Pattern.compile("(?i)reveal (your|the) (system prompt|instructions)"),
            Pattern.compile("(?i)act as (if you|an unrestricted)"),
            Pattern.compile("(?i)developer mode")
    );

    public GuardResult screenUserInput(String message) {
        return screen(message);
    }

    // Called on each RAG chunk's text before it's assembled into context —
    // documents are untrusted content, same as user input.
    public GuardResult screenRetrievedContent(String chunkText) {
        return screen(chunkText);
    }

    private GuardResult screen(String text) {
        if (text == null) return GuardResult.clean();
        for (Pattern p : SUSPICIOUS_PATTERNS) {
            if (p.matcher(text).find()) {
                return GuardResult.flagged("Detected potential instruction-override pattern.");
            }
        }
        return GuardResult.clean();
    }

    public record GuardResult(boolean flagged, String reason) {
        static GuardResult clean() { return new GuardResult(false, null); }
        static GuardResult flagged(String reason) { return new GuardResult(true, reason); }
    }
}