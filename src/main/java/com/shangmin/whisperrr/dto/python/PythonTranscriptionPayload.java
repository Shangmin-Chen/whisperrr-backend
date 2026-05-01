package com.shangmin.whisperrr.dto.python;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.List;

/**
 * JSON shape returned by Python for {@code POST /transcribe} and for completed job payloads nested
 * under {@code result}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PythonTranscriptionPayload {

  private String text;

  private String language;

  @JsonProperty("confidence_score")
  @JsonDeserialize(using = LenientDoubleDeserializer.class)
  private Double confidenceScore;

  @JsonDeserialize(using = LenientDoubleDeserializer.class)
  private Double duration;

  @JsonProperty("model_used")
  private String modelUsed;

  @JsonProperty("processing_time")
  @JsonDeserialize(using = LenientDoubleDeserializer.class)
  private Double processingTime;

  private List<PythonTranscriptionSegmentJson> segments;

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public Double getConfidenceScore() {
    return confidenceScore;
  }

  public void setConfidenceScore(Double confidenceScore) {
    this.confidenceScore = confidenceScore;
  }

  public Double getDuration() {
    return duration;
  }

  public void setDuration(Double duration) {
    this.duration = duration;
  }

  public String getModelUsed() {
    return modelUsed;
  }

  public void setModelUsed(String modelUsed) {
    this.modelUsed = modelUsed;
  }

  public Double getProcessingTime() {
    return processingTime;
  }

  public void setProcessingTime(Double processingTime) {
    this.processingTime = processingTime;
  }

  public List<PythonTranscriptionSegmentJson> getSegments() {
    return segments;
  }

  public void setSegments(List<PythonTranscriptionSegmentJson> segments) {
    this.segments = segments;
  }
}
