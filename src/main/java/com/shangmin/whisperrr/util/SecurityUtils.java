package com.shangmin.whisperrr.util;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

/**
 * Security utility class for input sanitization and validation.
 *
 * <p>This class provides methods to sanitize and validate user inputs to prevent security
 * vulnerabilities such as path traversal attacks, injection attacks, and other malicious input
 * patterns.
 *
 * <h3>Security Features:</h3>
 *
 * <ul>
 *   <li><strong>Path Traversal Protection:</strong> Prevents directory traversal attacks
 *   <li><strong>Filename Sanitization:</strong> Removes dangerous characters from filenames
 *   <li><strong>Input Validation:</strong> Validates input against safe patterns
 * </ul>
 *
 * @author shangmin
 * @version 1.0
 * @since 2024
 */
public class SecurityUtils {

  // Pattern to detect path traversal attempts
  private static final Pattern PATH_TRAVERSAL_PATTERN =
      Pattern.compile("(\\.\\./|\\.\\.\\\\|/|\\.\\.|%2e%2e%2f|%2e%2e%5c)");

  // Pattern for safe filename characters (alphanumeric, dash, underscore, dot)
  private static final Pattern SAFE_FILENAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]+$");

  // Maximum filename length
  private static final int MAX_FILENAME_LENGTH = 255;

  /**
   * Normalizes an upload filename for multipart usage: uses the path's last segment only, blocks
   * obvious traversal, preserves dots (e.g. {@code talk.part2.mp3}), and strips or replaces unsafe
   * characters.
   *
   * @param filename the raw filename from the client
   * @return safe basename, or null if unusable
   */
  public static String sanitizeFilename(String filename) {
    if (filename == null || filename.isEmpty()) {
      return null;
    }

    String trimmed = filename.trim();
    if (trimmed.isEmpty()) {
      return null;
    }

    if (trimmed.startsWith("..")
        || trimmed.contains("/../")
        || trimmed.contains("\\..\\")
        || trimmed.contains("/..\\")
        || trimmed.contains("\\../")) {
      return null;
    }

    final String nameOnly;
    try {
      nameOnly = Paths.get(trimmed).getFileName().toString();
    } catch (Exception e) {
      return null;
    }

    if (nameOnly.isEmpty() || ".".equals(nameOnly) || "..".equals(nameOnly)) {
      return null;
    }

    StringBuilder sb = new StringBuilder(nameOnly.length());
    for (int i = 0; i < nameOnly.length(); i++) {
      char c = nameOnly.charAt(i);
      if (c < 32 || c == 127) {
        continue;
      }
      if (c == '/' || c == '\\' || c == ':' || c == '*' || c == '?' || c == '"' || c == '<'
          || c == '>' || c == '|') {
        sb.append('_');
      } else {
        sb.append(c);
      }
    }

    String sanitized = sb.toString();
    if (sanitized.isBlank()) {
      return null;
    }
    if (sanitized.length() > MAX_FILENAME_LENGTH) {
      sanitized = sanitized.substring(0, MAX_FILENAME_LENGTH);
    }
    return sanitized;
  }

  /**
   * Validate that a filename is safe and doesn't contain path traversal patterns.
   *
   * @param filename the filename to validate
   * @return true if filename is safe, false otherwise
   */
  public static boolean isValidFilename(String filename) {
    if (filename == null || filename.isEmpty()) {
      return false;
    }

    // Check for path traversal patterns
    if (PATH_TRAVERSAL_PATTERN.matcher(filename).find()) {
      return false;
    }

    // Check length
    if (filename.length() > MAX_FILENAME_LENGTH) {
      return false;
    }

    // Extract just the filename (remove path if present)
    String nameOnly = Paths.get(filename).getFileName().toString();

    // Validate against safe pattern
    return SAFE_FILENAME_PATTERN.matcher(nameOnly).matches();
  }

  /**
   * Normalize a file path to prevent path traversal attacks.
   *
   * @param filePath the file path to normalize
   * @param baseDirectory the base directory that paths must be within
   * @return normalized path, or null if path is invalid or outside base directory
   */
  public static Path normalizePath(String filePath, Path baseDirectory) {
    if (filePath == null || filePath.isEmpty()) {
      return null;
    }

    try {
      Path path = Paths.get(filePath).normalize();
      Path resolved = baseDirectory.resolve(path).normalize();

      // Ensure resolved path is within base directory
      if (!resolved.startsWith(baseDirectory.normalize())) {
        return null;
      }

      return resolved;
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Extract file extension safely.
   *
   * @param filename the filename
   * @return file extension in lowercase, or empty string if no extension
   */
  public static String getFileExtension(String filename) {
    if (filename == null || filename.isEmpty()) {
      return "";
    }

    // Extract just the filename (remove path if present)
    String nameOnly = Paths.get(filename).getFileName().toString();

    int lastDotIndex = nameOnly.lastIndexOf('.');
    if (lastDotIndex == -1 || lastDotIndex == nameOnly.length() - 1) {
      return "";
    }

    return nameOnly.substring(lastDotIndex + 1).toLowerCase();
  }
}
