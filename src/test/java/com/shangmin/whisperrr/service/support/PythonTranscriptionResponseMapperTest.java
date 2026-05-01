package com.shangmin.whisperrr.service.support;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shangmin.whisperrr.dto.TranscriptionResultResponse;
import com.shangmin.whisperrr.dto.TranscriptionSegment;
import com.shangmin.whisperrr.dto.python.PythonTranscriptionPayload;
import java.io.InputStream;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PythonTranscriptionResponseMapperTest {

  private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

  private PythonTranscriptionResponseMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = new PythonTranscriptionResponseMapper();
  }

  @Test
  void resolveTranscriptionText_prefersTopLevelText() throws Exception {
    PythonTranscriptionPayload p = readFixture("transcription_text_only.json");
    assertEquals("Hello world", mapper.resolveTranscriptionText(p));
  }

  @Test
  void resolveTranscriptionText_joinsSegmentsWhenTextMissing() throws Exception {
    PythonTranscriptionPayload p = readFixture("transcription_segments_only.json");
    assertEquals("Bonjour le monde", mapper.resolveTranscriptionText(p));
  }

  @Test
  void toSegments_mapsAvgLogprobToConfidence() throws Exception {
    PythonTranscriptionPayload p = readFixture("transcription_segment_avg_logprob.json");
    List<TranscriptionSegment> segments = mapper.toSegments(p);
    assertEquals(1, segments.size());
    assertEquals("one", segments.getFirst().getText());
    assertEquals(0.25, segments.getFirst().getConfidence(), 1e-9);
    assertEquals(0.0, segments.getFirst().getStartTime());
    assertEquals(1.0, segments.getFirst().getEndTime());
  }

  @Test
  void toSegments_skipsWhitespaceOnlySegmentText() throws Exception {
    PythonTranscriptionPayload p = readFixture("transcription_trim_segments.json");
    List<TranscriptionSegment> segments = mapper.toSegments(p);
    assertEquals(1, segments.size());
    assertEquals("real", segments.getFirst().getText());
  }

  @Test
  void toTranscriptionApiResponse_combinesTextSegmentsAndMetadata() throws Exception {
    PythonTranscriptionPayload p = readFixture("transcription_text_only.json");
    TranscriptionResultResponse out = mapper.toTranscriptionApiResponse(p);
    assertEquals("Hello world", out.getTranscriptionText());
    assertEquals("en", out.getLanguage());
    assertEquals(0.95, out.getConfidence());
    assertEquals(5.5, out.getDuration());
    assertEquals("base", out.getModelUsed());
    assertEquals(2.3, out.getProcessingTime());
    assertNotNull(out.getCompletedAt());
    assertNotNull(out.getStatus());
    assertTrue(out.getSegments().isEmpty());
  }

  private PythonTranscriptionPayload readFixture(String name) throws Exception {
    String path = "/fixtures/python/" + name;
    try (InputStream in = PythonTranscriptionResponseMapperTest.class.getResourceAsStream(path)) {
      assertNotNull(in, "Missing classpath resource: " + path);
      return objectMapper.readValue(in, PythonTranscriptionPayload.class);
    }
  }
}
