package com.zenvora.agents.ch03;

/** The model's reply could not be understood as a decision. */
public class InvalidDecisionException extends RuntimeException {

    public InvalidDecisionException(String message) {
        super(message);
    }
}
