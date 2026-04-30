package com.shangmin.whisperrr.enums;

/**
 * Enumeration of supported audio formats for transcription processing.
 * 
 * <p>This enum defines all audio formats that the Whisperrr transcription system
 * can accept and process. Each format includes both the MIME type for HTTP
 * content type validation and the file extension for filename validation.</p>
 * 
 * <h3>Supported Formats:</h3>
 * <ul>
 *   <li><strong>MP3:</strong> Most common compressed audio format, widely supported</li>
 *   <li><strong>WAV:</strong> Uncompressed audio format, high quality</li>
 *   <li><strong>M4A:</strong> Apple's compressed audio format, good quality</li>
 *   <li><strong>FLAC:</strong> Lossless compressed audio format, excellent quality</li>
 *   <li><strong>OGG:</strong> Open-source compressed audio format</li>
 * </ul>
 * 
 * <h3>Format Selection Guidelines:</h3>
 * <ul>
 *   <li><strong>Best Quality:</strong> WAV or FLAC for highest transcription accuracy</li>
 *   <li><strong>Best Compatibility:</strong> MP3 for widest device support</li>
 *   <li><strong>Best Compression:</strong> M4A or OGG for smaller file sizes</li>
 * </ul>
 * 
 * <h3>Validation Usage:</h3>
 * <p>This enum is used throughout the system for:</p>
 * <ul>
 *   <li><strong>File Upload Validation:</strong> Checking file extensions and MIME types</li>
 *   <li><strong>Database Storage:</strong> Storing format information with audio files</li>
 *   <li><strong>Processing Pipeline:</strong> Format-specific handling in transcription</li>
 *   <li><strong>Client Communication:</strong> Informing clients of supported formats</li>
 * </ul>
 * 
 * <h3>Whisper Compatibility:</h3>
 * <p>All supported formats are compatible with OpenAI's Whisper transcription
 * model, which can handle various audio formats and automatically convert them
 * to the required format for processing.</p>
 * 
 * <h3>Quality Considerations:</h3>
 * <ul>
 *   <li><strong>Sample Rate:</strong> Higher sample rates (44.1kHz+) provide better results</li>
 *   <li><strong>Bit Depth:</strong> 16-bit or higher recommended for quality</li>
 *   <li><strong>Compression:</strong> Lossless formats preferred for critical transcriptions</li>
 *   <li><strong>Mono vs Stereo:</strong> Mono sufficient for speech, stereo for music</li>
 * </ul>
 * 
 * @author shangmin
 * @version 1.0
 * @since 2024
 * 
 * @see com.shangmin.whisperrr.entity.AudioFile
 * @see com.shangmin.whisperrr.service.AudioService#validateAudioFile(MultipartFile)
 */
public enum AudioFormat {
    /**
     * MP3 (MPEG Audio Layer III) - Most widely supported compressed audio format.
     * 
     * <p>Lossy compression format that provides good quality at smaller file sizes.
     * Excellent compatibility across all devices and platforms. Recommended for
     * general use where file size is a concern.</p>
     */
    MP3("audio/mpeg", "mp3"),
    
    /**
     * WAV (Waveform Audio File Format) - Uncompressed audio format.
     * 
     * <p>Lossless audio format that preserves original quality. Larger file sizes
     * but provides the best transcription accuracy. Recommended for high-quality
     * recordings and critical transcriptions.</p>
     */
    WAV("audio/wav", "wav"),
    
    /**
     * M4A (MPEG-4 Audio) - Apple's compressed audio format.
     * 
     * <p>Advanced Audio Coding (AAC) format with good compression and quality.
     * Native format for Apple devices. Provides better quality than MP3 at
     * similar file sizes.</p>
     */
    M4A("audio/mp4", "m4a"),
    
    /**
     * FLAC (Free Lossless Audio Codec) - Lossless compressed audio format.
     * 
     * <p>Open-source lossless compression that reduces file size while preserving
     * perfect audio quality. Ideal for archival and high-quality transcriptions
     * where both quality and storage efficiency matter.</p>
     */
    FLAC("audio/flac", "flac"),
    
    /**
     * OGG (Ogg Vorbis) - Open-source compressed audio format.
     * 
     * <p>Free and open-source lossy compression format. Provides good quality
     * and compression efficiency. Alternative to MP3 without patent restrictions.</p>
     */
    OGG("audio/ogg", "ogg");
    
    private final String mimeType;
    private final String extension;
    
    /**
     * Constructor for AudioFormat enum values.
     * 
     * <p>Each audio format is defined with its corresponding MIME type and
     * file extension for comprehensive validation during file upload.</p>
     * 
     * @param mimeType the standard MIME type for HTTP content type validation
     *                 Used to validate the Content-Type header of uploaded files
     * @param extension the file extension without the dot (e.g., "mp3", not ".mp3")
     *                  Used for filename validation and format detection
     */
    AudioFormat(String mimeType, String extension) {
        this.mimeType = mimeType;
        this.extension = extension;
    }
    
    /**
     * Gets the MIME type for this audio format.
     * 
     * <p>The MIME type is used for HTTP content type validation when files
     * are uploaded. It ensures that the browser-reported content type matches
     * the expected format for the file extension.</p>
     * 
     * <h4>Common MIME Types:</h4>
     * <ul>
     *   <li>audio/mpeg - MP3 files</li>
     *   <li>audio/wav - WAV files</li>
     *   <li>audio/mp4 - M4A files</li>
     *   <li>audio/flac - FLAC files</li>
     *   <li>audio/ogg - OGG files</li>
     * </ul>
     * 
     * @return String the standard MIME type for this audio format
     */
    public String getMimeType() {
        return mimeType;
    }
    
    /**
     * Gets the file extension for this audio format.
     * 
     * <p>The file extension is used for filename validation and format
     * detection. It's stored without the leading dot for consistency
     * and ease of use in validation logic.</p>
     * 
     * @return String the file extension without the dot (e.g., "mp3", "wav")
     */
    public String getExtension() {
        return extension;
    }
    
    /**
     * Gets the AudioFormat enum value from a file extension.
     * 
     * <p>This method performs case-insensitive matching and handles extensions
     * with or without the leading dot. It's commonly used during file upload
     * validation to determine the format from the filename.</p>
     * 
     * <h4>Supported Input Formats:</h4>
     * <ul>
     *   <li>With dot: ".mp3", ".wav", ".m4a", ".flac", ".ogg"</li>
     *   <li>Without dot: "mp3", "wav", "m4a", "flac", "ogg"</li>
     *   <li>Case insensitive: "MP3", "Wav", "M4A", etc.</li>
     * </ul>
     * 
     * @param extension the file extension to look up, with or without leading dot
     *                  Can be null (returns null), empty (returns null), or any case
     * @return AudioFormat enum value if the extension is supported,
     *         null if the extension is not recognized or is null/empty
     * 
     * @see #isSupported(String)
     */
    public static AudioFormat fromExtension(String extension) {
        if (extension == null) {
            return null;
        }
        
        // Remove dot if present
        String cleanExtension = extension.startsWith(".") ? extension.substring(1) : extension;
        
        for (AudioFormat format : values()) {
            if (format.extension.equalsIgnoreCase(cleanExtension)) {
                return format;
            }
        }
        return null;
    }
    
    /**
     * Gets the AudioFormat enum value from a MIME type.
     * 
     * <p>This method performs case-insensitive matching against the standard
     * MIME types for audio formats. It's used during file upload validation
     * to verify that the Content-Type header matches a supported format.</p>
     * 
     * <h4>Recognized MIME Types:</h4>
     * <ul>
     *   <li>audio/mpeg (MP3)</li>
     *   <li>audio/wav (WAV)</li>
     *   <li>audio/mp4 (M4A)</li>
     *   <li>audio/flac (FLAC)</li>
     *   <li>audio/ogg (OGG)</li>
     * </ul>
     * 
     * @param mimeType the MIME type string to look up
     *                 Can be null (returns null), empty (returns null), or any case
     * @return AudioFormat enum value if the MIME type is supported,
     *         null if the MIME type is not recognized or is null/empty
     * 
     * @see #getMimeType()
     */
    public static AudioFormat fromMimeType(String mimeType) {
        if (mimeType == null) {
            return null;
        }
        
        for (AudioFormat format : values()) {
            if (format.mimeType.equalsIgnoreCase(mimeType)) {
                return format;
            }
        }
        return null;
    }
    
    /**
     * Checks if the given file extension is supported by the system.
     * 
     * <p>This is a convenience method that combines extension lookup and
     * null checking. It's useful for quick validation without needing
     * to handle the returned enum value.</p>
     * 
     * <h4>Usage Examples:</h4>
     * <pre>
     * AudioFormat.isSupported("mp3")    // returns true
     * AudioFormat.isSupported(".wav")   // returns true
     * AudioFormat.isSupported("MP3")    // returns true (case insensitive)
     * AudioFormat.isSupported("txt")    // returns false
     * AudioFormat.isSupported(null)     // returns false
     * </pre>
     * 
     * @param extension the file extension to check, with or without leading dot
     * @return true if the extension corresponds to a supported audio format,
     *         false if the extension is not supported, null, or empty
     * 
     * @see #fromExtension(String)
     */
    public static boolean isSupported(String extension) {
        return fromExtension(extension) != null;
    }
}
