package com.shangmin.whisperrr.exception;

/**
 * Exception thrown when transcription processing fails
 */
public class TranscriptionProcessingException extends RuntimeException {
    
    public TranscriptionProcessingException(String message) {
        super(message);
    }
    
    public TranscriptionProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
