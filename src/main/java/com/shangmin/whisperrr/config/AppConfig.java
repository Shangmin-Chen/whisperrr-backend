package com.shangmin.whisperrr.config;

/**
 * Centralized application configuration constants.
 *
 * <p>Upload format allowlists live on {@link com.shangmin.whisperrr.enums.AudioFormat}; this class
 * holds size limits and other app-wide constants only.
 */
public final class AppConfig {

  /** Maximum file size: 50MB in bytes. */
  public static final long MAX_FILE_SIZE_BYTES = 50L * 1024 * 1024;

  /** Maximum file size in MB. */
  public static final int MAX_FILE_SIZE_MB = 50;

  private AppConfig() {}
}
