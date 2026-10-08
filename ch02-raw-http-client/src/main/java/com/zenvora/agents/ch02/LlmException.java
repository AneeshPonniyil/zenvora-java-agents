package com.zenvora.agents.ch02;

/** Thrown when a model call fails. {@code status} is the HTTP status, or -1 for network errors. */
public class LlmException extends RuntimeException {

    private final int status;

    public LlmException(String message, int status) {
        super(message);
        this.status = status;
    }

    public LlmException(String message, int status, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public int status() {
        return status;
    }
}
