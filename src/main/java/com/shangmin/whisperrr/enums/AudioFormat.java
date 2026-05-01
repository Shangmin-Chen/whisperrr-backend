package com.shangmin.whisperrr.enums;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Supported upload formats for transcription: audio codecs and common video containers (extracted
 * downstream).
 *
 * <p>Extension and MIME checks both run against this enum — update values here only when widening
 * or tightening what the API accepts.
 */
public enum AudioFormat {
  MP3(Category.AUDIO, "audio/mpeg", "mp3"),
  WAV(Category.AUDIO, "audio/wav", "wav"),
  M4A(Category.AUDIO, "audio/mp4", "m4a"),
  FLAC(Category.AUDIO, "audio/flac", "flac"),
  OGG(Category.AUDIO, "audio/ogg", "ogg"),
  WMA(Category.AUDIO, "audio/x-ms-wma", "wma"),
  AAC(Category.AUDIO, "audio/aac", "aac"),

  MP4(Category.VIDEO, "video/mp4", "mp4"),
  AVI(Category.VIDEO, "video/x-msvideo", "avi"),
  MOV(Category.VIDEO, "video/quicktime", "mov"),
  MKV(Category.VIDEO, "video/x-matroska", "mkv"),
  FLV(Category.VIDEO, "video/x-flv", "flv"),
  WEBM(Category.VIDEO, "video/webm", "webm"),
  WMV(Category.VIDEO, "video/x-ms-wmv", "wmv"),
  M4V(Category.VIDEO, "video/x-m4v", "m4v"),
  THREE_GP(Category.VIDEO, "video/3gpp", "3gp");

  private enum Category {
    AUDIO,
    VIDEO
  }

  private final Category category;
  private final String mimeType;
  private final String extension;

  AudioFormat(Category category, String mimeType, String extension) {
    this.category = category;
    this.mimeType = mimeType;
    this.extension = extension;
  }

  public String getMimeType() {
    return mimeType;
  }

  public String getExtension() {
    return extension;
  }

  /**
   * Whether the HTTP Content-Type (possibly including parameters) matches this format: primary MIME
   * equality, else {@code audio/*} or {@code video/*} aligned with this format's category.
   */
  public boolean isCompatibleContentType(String contentType) {
    if (contentType == null || contentType.isBlank()) {
      return false;
    }
    String base = contentType.split(";", 2)[0].trim();
    if (base.equalsIgnoreCase(mimeType)) {
      return true;
    }
    String lower = base.toLowerCase();
    return switch (category) {
      case AUDIO -> lower.startsWith("audio/");
      case VIDEO -> lower.startsWith("video/");
    };
  }

  public static AudioFormat fromExtension(String extension) {
    if (extension == null || extension.isBlank()) {
      return null;
    }
    String clean = extension.startsWith(".") ? extension.substring(1) : extension;
    for (AudioFormat format : values()) {
      if (format.extension.equalsIgnoreCase(clean)) {
        return format;
      }
    }
    return null;
  }

  public static AudioFormat fromMimeType(String mimeType) {
    if (mimeType == null || mimeType.isBlank()) {
      return null;
    }
    String base = mimeType.split(";", 2)[0].trim();
    for (AudioFormat format : values()) {
      if (format.mimeType.equalsIgnoreCase(base)) {
        return format;
      }
    }
    return null;
  }

  /** Comma-separated sorted extensions for validation error messages. */
  public static String supportedExtensionsListing() {
    return Arrays.stream(values())
        .map(AudioFormat::getExtension)
        .sorted()
        .collect(Collectors.joining(", "));
  }
}
