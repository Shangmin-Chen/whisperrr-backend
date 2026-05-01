package com.shangmin.whisperrr.dto.python;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

/** Verifies lenient coercion for Python-side payloads that drift from strict numeric JSON. */
class PythonTranscriptionPayloadDeserializationTest {

  private final ObjectMapper mapper =
      new ObjectMapper().findAndRegisterModules(); // JDK 8 dates if used elsewhere on same mapper

  @Test
  void malformedConfidenceStringsBecomeNull() throws Exception {
    String json =
        """
        {"text":"x","language":"en","confidence_score":"invalid"}
        """;
    PythonTranscriptionPayload p = mapper.readValue(json, PythonTranscriptionPayload.class);
    assertEquals("x", p.getText());
    assertNull(p.getConfidenceScore());
  }

  @Test
  void numericConfidenceStringsDeserialize() throws Exception {
    String json =
        "{\"text\":\"x\",\"language\":\"en\",\"confidence_score\":\"0.95\",\"duration\":\"5.5\"}";
    PythonTranscriptionPayload p = mapper.readValue(json, PythonTranscriptionPayload.class);
    assertEquals(0.95, p.getConfidenceScore());
    assertEquals(5.5, p.getDuration());
  }

  @Test
  void segmentAcceptsAlternativeTimeKeysAndAvgLogprob() throws Exception {
    String json =
        """
        {"segments":[{"start":"1","end":"2","text":"hi","avg_logprob":"-0.5"}]}
        """;
    PythonTranscriptionPayload p = mapper.readValue(json, PythonTranscriptionPayload.class);
    assertNotNull(p.getSegments());
    assertEquals(1.0, p.getSegments().getFirst().getStartTime());
    assertEquals(2.0, p.getSegments().getFirst().getEndTime());
    assertEquals("hi", p.getSegments().getFirst().getText());
    assertEquals(-0.5, p.getSegments().getFirst().getAvgLogprob());
    assertNull(p.getSegments().getFirst().getConfidence());
  }
}
