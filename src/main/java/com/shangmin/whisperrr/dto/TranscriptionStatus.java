package com.shangmin.whisperrr.dto;

/**
 * Enum representing the simplified transcription status.
 * 
 * <p>This enum is used in TranscriptionResultResponse to indicate the outcome
 * of a direct transcription operation. In the simplified architecture, only
 * terminal states (COMPLETED and FAILED) are used since processing is synchronous.</p>
 * 
 * <h3>Status Values:</h3>
 * <ul>
 *   <li><strong>COMPLETED:</strong> Transcription finished successfully</li>
 *   <li><strong>FAILED:</strong> Transcription processing failed</li>
 * </ul>
 * 
 * <p><strong>Note:</strong> PENDING and PROCESSING states are not used in the
 * simplified direct processing flow, but are maintained for type compatibility.</p>
 * 
 * @author shangmin
 * @version 2.0
 * @since 2024
 */
public enum TranscriptionStatus {
    /** Transcription completed successfully */
    COMPLETED,
    
    /** Transcription processing failed */
    FAILED,
    
    /** 
     * Not used in simplified architecture - maintained for compatibility
     * @deprecated Not used in direct processing flow
     */
    @Deprecated
    PENDING,
    
    /** 
     * Not used in simplified architecture - maintained for compatibility
     * @deprecated Not used in direct processing flow
     */
    @Deprecated
    PROCESSING
}
