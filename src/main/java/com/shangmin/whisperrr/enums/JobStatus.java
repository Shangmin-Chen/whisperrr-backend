package com.shangmin.whisperrr.enums;

/**
 * Enumeration representing the simplified status of a transcription result.
 * 
 * <p>This enum defines the possible outcomes of a direct transcription operation
 * in the simplified Whisperrr architecture. Since the system processes transcriptions
 * immediately without job queuing, only terminal states are needed.</p>
 * 
 * <h3>Simplified Architecture:</h3>
 * <p>The system uses direct processing without job management:</p>
 * <ul>
 *   <li><strong>No Job Queuing:</strong> Files are processed immediately upon upload</li>
 *   <li><strong>No Polling Required:</strong> Results are returned synchronously</li>
 *   <li><strong>No Status Tracking:</strong> Only final outcomes are represented</li>
 * </ul>
 * 
 * <h3>Status Values:</h3>
 * <ul>
 *   <li><strong>COMPLETED:</strong> Transcription finished successfully with results available</li>
 *   <li><strong>FAILED:</strong> Processing encountered an error and could not complete</li>
 * </ul>
 * 
 * <h3>Usage:</h3>
 * <p>This enum is used in TranscriptionResultResponse to indicate the outcome
 * of a direct transcription operation. All successful transcriptions will have
 * COMPLETED status, while errors will result in FAILED status.</p>
 * 
 * <p><strong>Note:</strong> This enum is maintained for backward compatibility and
 * API consistency. The simplified system only uses COMPLETED and FAILED states.
 * PENDING, PROCESSING, and CANCELLED states are not used in the direct processing flow.</p>
 * 
 * @author shangmin
 * @version 2.0
 * @since 2024
 * 
 * @see com.shangmin.whisperrr.dto.TranscriptionResultResponse
 * @see com.shangmin.whisperrr.dto.TranscriptionStatus
 */
public enum JobStatus {
    /**
     * Transcription has completed successfully with results available.
     * 
     * <p>In the simplified direct processing architecture, this status indicates
     * that the transcription finished successfully and results are immediately
     * available in the response. No polling or additional requests are needed.</p>
     */
    COMPLETED("Successfully completed"),
    
    /**
     * Transcription processing failed due to an error.
     * 
     * <p>An error occurred during transcription processing. The error message
     * should provide details about what went wrong. The user may retry with
     * a different file or after resolving the issue.</p>
     */
    FAILED("Processing failed"),
    
    /**
     * Job has been created and is waiting in the queue for processing.
     * 
     * <p><strong>Note:</strong> This state is not used in the simplified direct
     * processing architecture. It is maintained for backward compatibility only.</p>
     */
    PENDING("Pending processing"),
    
    /**
     * Job is currently being processed by the transcription service.
     * 
     * <p><strong>Note:</strong> This state is not used in the simplified direct
     * processing architecture. It is maintained for backward compatibility only.</p>
     */
    PROCESSING("Currently processing"),
    
    /**
     * Job was cancelled before completion.
     * 
     * <p><strong>Note:</strong> This state is not used in the simplified direct
     * processing architecture. It is maintained for backward compatibility only.</p>
     */
    CANCELLED("Job cancelled");
    
    private final String description;
    
    /**
     * Constructor for JobStatus enum values.
     * 
     * <p>Each enum value is initialized with a human-readable description
     * that can be displayed to users or used in logging and error messages.</p>
     * 
     * @param description human-readable description of the job status
     *                   Used for user interfaces and logging
     */
    JobStatus(String description) {
        this.description = description;
    }
    
    /**
     * Gets the human-readable description of the job status.
     * 
     * <p>This description is suitable for displaying to end users in
     * user interfaces, status messages, and notifications. It provides
     * clear, understandable information about the current job state.</p>
     * 
     * @return String user-friendly description of the current status
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Checks if the transcription is in a terminal state.
     * 
     * <p>In the simplified architecture, all responses are terminal states
     * since processing is synchronous. This method is maintained for backward
     * compatibility and API consistency.</p>
     * 
     * <h4>Terminal States:</h4>
     * <ul>
     *   <li><strong>COMPLETED:</strong> Transcription finished successfully</li>
     *   <li><strong>FAILED:</strong> Transcription failed with an error</li>
     * </ul>
     * 
     * <p><strong>Note:</strong> In the direct processing flow, all responses
     * are terminal states. No polling is required.</p>
     * 
     * @return true if the transcription is in a terminal state (COMPLETED or FAILED),
     *         false otherwise
     */
    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED;
    }
    
    /**
     * Checks if the transcription is in an active processing state.
     * 
     * <p><strong>Note:</strong> This method is not used in the simplified direct
     * processing architecture, as all processing is synchronous. It is maintained
     * for backward compatibility only.</p>
     * 
     * @return false in the simplified architecture (no active processing states)
     */
    public boolean isProcessing() {
        return false; // No processing states in simplified architecture
    }
}
