package com.shangmin.whisperrr.dto.python;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deserializes numeric JSON values into {@link Double}; non-numeric primitives become null instead
 * of failing the entire response (mirrors legacy {@code instanceof Number} checks).
 */
public class LenientDoubleDeserializer extends JsonDeserializer<Double> {

  private static final Logger log = LoggerFactory.getLogger(LenientDoubleDeserializer.class);

  @Override
  public Double deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    switch (p.currentToken()) {
      case VALUE_NULL:
        return null;
      case VALUE_NUMBER_FLOAT:
      case VALUE_NUMBER_INT:
        return p.getDoubleValue();
      case VALUE_STRING:
        try {
          return Double.parseDouble(p.getText().trim());
        } catch (NumberFormatException e) {
          log.warn("Ignoring non-numeric string for Double field");
          return null;
        }
      default:
        return null;
    }
  }
}
