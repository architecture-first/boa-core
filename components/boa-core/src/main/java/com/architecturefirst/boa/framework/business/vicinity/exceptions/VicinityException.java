package com.architecturefirst.boa.framework.business.vicinity.exceptions;

/**
 * Represents an exception during Vicinity processing
 */
public class VicinityException extends RuntimeException {
    public VicinityException(Exception e) {
        super(e);
    }

    public VicinityException(String message) {
        super(message);
    }
}
