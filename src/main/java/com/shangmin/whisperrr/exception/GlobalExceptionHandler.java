package com.shangmin.whisperrr.exception;

import com.shangmin.whisperrr.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * Global exception handler with correlation ID support and sanitized error messages.
 *
 * <p>Upstream HTTP calls to the Python transcription service translate {@link
 * org.springframework.web.client.RestClient} failures into {@link TranscriptionProcessingException}
 * inside the service layer; those failures are surfaced here rather than via separate handlers for
 * {@code ResourceAccessException}, {@link org.springframework.web.client.HttpClientErrorException},
 * or {@link org.springframework.web.client.HttpServerErrorException}.
 *
 * @author shangmin
 * @version 1.0
 * @since 2024
 */
@ControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  /**
   * Get correlation ID from request context or MDC.
   *
   * @param request the HTTP request
   * @return correlation ID, or "unknown" if not found
   */
  private String getCorrelationId(HttpServletRequest request) {
    String correlationId = MDC.get("correlationId");
    if (correlationId == null) {
      correlationId = (String) request.getAttribute("correlationId");
    }
    return correlationId != null ? correlationId : "unknown";
  }

  /**
   * Sanitize error messages to prevent information leakage.
   *
   * @param message the error message to sanitize
   * @return sanitized error message
   */
  private String sanitizeErrorMessage(String message) {
    if (message == null) return "An error occurred";
    return message
        .replaceAll("(/[^\\s]+)", "[path]")
        .replaceAll("(at [^\\s]+\\.[^\\s]+)", "[location]")
        .replaceAll("(Exception|Error):", "");
  }

  @ExceptionHandler(FileValidationException.class)
  public ResponseEntity<ErrorResponse> handleFileValidationException(
      FileValidationException ex, HttpServletRequest request) {
    String correlationId = getCorrelationId(request);
    logger.warn("File validation error [{}]: {}", correlationId, ex.getMessage());
    ErrorResponse error =
        new ErrorResponse(
            "FILE_VALIDATION_ERROR",
            sanitizeErrorMessage(ex.getMessage()),
            LocalDateTime.now(),
            correlationId);
    return ResponseEntity.badRequest().body(error);
  }

  @ExceptionHandler(JobNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleJobNotFoundException(
      JobNotFoundException ex, HttpServletRequest request) {
    String correlationId = getCorrelationId(request);
    logger.warn("Job not found [{}]: {}", correlationId, ex.getMessage());
    ErrorResponse error =
        new ErrorResponse(
            "JOB_NOT_FOUND",
            sanitizeErrorMessage(ex.getMessage()),
            LocalDateTime.now(),
            correlationId);
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }

  @ExceptionHandler(TranscriptionProcessingException.class)
  public ResponseEntity<ErrorResponse> handleTranscriptionProcessingException(
      TranscriptionProcessingException ex, HttpServletRequest request) {
    String correlationId = getCorrelationId(request);
    logger.error("Transcription processing error [{}]: {}", correlationId, ex.getMessage(), ex);
    ErrorResponse error =
        new ErrorResponse(
            "TRANSCRIPTION_PROCESSING_ERROR",
            "Transcription processing failed. Please try again later.",
            LocalDateTime.now(),
            correlationId);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    String correlationId = getCorrelationId(request);
    logger.warn("Validation error [{}]: {}", correlationId, ex.getMessage());
    String message =
        ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .findFirst()
            .orElse("Validation failed");

    ErrorResponse error =
        new ErrorResponse(
            "VALIDATION_ERROR", sanitizeErrorMessage(message), LocalDateTime.now(), correlationId);
    return ResponseEntity.badRequest().body(error);
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(
      MaxUploadSizeExceededException ex, HttpServletRequest request) {
    String correlationId = getCorrelationId(request);
    logger.warn("File size exceeded limit [{}]: {}", correlationId, ex.getMessage());
    ErrorResponse error =
        new ErrorResponse(
            "FILE_SIZE_EXCEEDED",
            "File size exceeds the maximum allowed limit of 50MB",
            LocalDateTime.now(),
            correlationId);
    return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(error);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGenericException(
      Exception ex, HttpServletRequest request) {
    String correlationId = getCorrelationId(request);
    logger.error("Unexpected error occurred [{}]: {}", correlationId, ex.getMessage(), ex);
    ErrorResponse error =
        new ErrorResponse(
            "INTERNAL_SERVER_ERROR",
            "An unexpected error occurred",
            LocalDateTime.now(),
            correlationId);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
  }
}
