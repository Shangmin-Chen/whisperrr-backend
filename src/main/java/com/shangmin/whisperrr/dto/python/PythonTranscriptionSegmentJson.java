package com.shangmin.whisperrr.dto.python;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PythonTranscriptionSegmentJson {

  @JsonProperty("start_time")
  @JsonAlias({"start"})
  @JsonDeserialize(using = LenientDoubleDeserializer.class)
  private Double startTime;

  @JsonProperty("end_time")
  @JsonAlias({"end"})
  @JsonDeserialize(using = LenientDoubleDeserializer.class)
  private Double endTime;

  private String text;

  @JsonDeserialize(using = LenientDoubleDeserializer.class)
  private Double confidence;

  @JsonProperty("avg_logprob")
  @JsonDeserialize(using = LenientDoubleDeserializer.class)
  private Double avgLogprob;

  public Double getStartTime() {
    return startTime;
  }

  public void setStartTime(Double startTime) {
    this.startTime = startTime;
  }

  public Double getEndTime() {
    return endTime;
  }

  public void setEndTime(Double endTime) {
    this.endTime = endTime;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public Double getConfidence() {
    return confidence;
  }

  public void setConfidence(Double confidence) {
    this.confidence = confidence;
  }

  public Double getAvgLogprob() {
    return avgLogprob;
  }

  public void setAvgLogprob(Double avgLogprob) {
    this.avgLogprob = avgLogprob;
  }
}
