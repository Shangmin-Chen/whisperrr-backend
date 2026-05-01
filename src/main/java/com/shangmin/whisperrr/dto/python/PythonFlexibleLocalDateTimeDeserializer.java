package com.shangmin.whisperrr.dto.python;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Parses timestamps from the Python job API; drops explicit offset/Z before applying ISO local
 * parsing (behavior aligned with prior client-side handling).
 */
public class PythonFlexibleLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

  @Override
  public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    if (!p.currentToken().isScalarValue()) {
      return null;
    }
    String raw = p.getValueAsString();
    if (raw == null) {
      return null;
    }
    raw = raw.trim();
    if (raw.isEmpty()) {
      return null;
    }
    String normalized = raw;
    if (normalized.contains("T")) {
      int plus = normalized.indexOf('+');
      if (plus > 0 && plus > normalized.indexOf('T')) {
        normalized = normalized.substring(0, plus);
      } else if (normalized.endsWith("Z")) {
        normalized = normalized.substring(0, normalized.length() - 1);
      }
    }
    try {
      return LocalDateTime.parse(normalized, FORMATTER);
    } catch (DateTimeParseException ignored) {
      try {
        return LocalDateTime.parse(raw, FORMATTER);
      } catch (DateTimeParseException e2) {
        return null;
      }
    }
  }
}
