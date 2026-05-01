package com.shangmin.whisperrr.dto.python;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PythonJobProgressPayload {

  @JsonProperty("job_id")
  private String jobId;

  private String status;

  @JsonDeserialize(using = LenientDoubleDeserializer.class)
  private Double progress;

  private String message;

  private PythonTranscriptionPayload result;

  private Object error;

  @JsonProperty("created_at")
  @JsonDeserialize(using = PythonFlexibleLocalDateTimeDeserializer.class)
  private LocalDateTime createdAt;

  @JsonProperty("updated_at")
  @JsonDeserialize(using = PythonFlexibleLocalDateTimeDeserializer.class)
  private LocalDateTime updatedAt;

  public String getJobId() {
    return jobId;
  }

  public void setJobId(String jobId) {
    this.jobId = jobId;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Double getProgress() {
    return progress;
  }

  public void setProgress(Double progress) {
    this.progress = progress;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public PythonTranscriptionPayload getResult() {
    return result;
  }

  public void setResult(PythonTranscriptionPayload result) {
    this.result = result;
  }

  public Object getError() {
    return error;
  }

  public void setError(Object error) {
    this.error = error;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}
