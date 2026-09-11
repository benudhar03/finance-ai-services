package com.finance.ai.exception;

public class PromptGuardException extends RuntimeException {

    public PromptGuardException(String message) {
        super(message);
    }

    public PromptGuardException(String message, Throwable cause) {
        super(message, cause);
    }
}