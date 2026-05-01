package com.shangmin.whisperrr.service.support;

import com.shangmin.whisperrr.dto.JobProgressResponse;
import com.shangmin.whisperrr.dto.JobSubmissionResponse;
import com.shangmin.whisperrr.dto.TranscriptionResultResponse;
import com.shangmin.whisperrr.dto.TranscriptionSegment;
import com.shangmin.whisperrr.dto.TranscriptionStatus;
import com.shangmin.whisperrr.dto.python.PythonJobProgressPayload;
import com.shangmin.whisperrr.dto.python.PythonJobSubmitPayload;
import com.shangmin.whisperrr.dto.python.PythonTranscriptionPayload;
import com.shangmin.whisperrr.dto.python.PythonTranscriptionSegmentJson;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Maps Python JSON payloads (deserialized into {@link PythonTranscriptionPayload} and related DTOs)
 * into API-facing responses.
 */
@Component
public class PythonTranscriptionResponseMapper {

  public String resolveTranscriptionText(PythonTranscriptionPayload payload) {
    if (payload == null) {
      return "";
    }
    String text = payload.getText() != null ? payload.getText().trim() : "";
    if (!text.isEmpty()) {
      return text;
    }
    List<PythonTranscriptionSegmentJson> segments = payload.getSegments();
    if (segments == null || segments.isEmpty()) {
      return "";
    }
    StringBuilder textBuilder = new StringBuilder();
    for (PythonTranscriptionSegmentJson segment : segments) {
      String segText = segment.getText() != null ? segment.getText().trim() : "";
      if (!segText.isEmpty()) {
        if (textBuilder.length() > 0) {
          textBuilder.append(' ');
        }
        textBuilder.append(segText);
      }
    }
    return textBuilder.toString();
  }

  public List<TranscriptionSegment> toSegments(PythonTranscriptionPayload payload) {
    if (payload == null || payload.getSegments() == null) {
      return Collections.emptyList();
    }
    List<TranscriptionSegment> segments = new ArrayList<>();
    for (PythonTranscriptionSegmentJson s : payload.getSegments()) {
      TranscriptionSegment out = segmentFromPython(s);
      if (out.getText() != null && !out.getText().isEmpty()) {
        segments.add(out);
      }
    }
    return segments;
  }

  private static TranscriptionSegment segmentFromPython(PythonTranscriptionSegmentJson py) {
    TranscriptionSegment segment = new TranscriptionSegment();
    if (py.getStartTime() != null) {
      segment.setStartTime(py.getStartTime());
    }
    if (py.getEndTime() != null) {
      segment.setEndTime(py.getEndTime());
    }
    if (py.getText() != null && !py.getText().trim().isEmpty()) {
      segment.setText(py.getText().trim());
    }
    if (py.getConfidence() != null) {
      segment.setConfidence(py.getConfidence());
    } else if (py.getAvgLogprob() != null) {
      double avg = py.getAvgLogprob();
      segment.setConfidence(Math.max(0, Math.min(1, (avg + 1) / 2)));
    }
    return segment;
  }

  public TranscriptionResultResponse toTranscriptionApiResponse(PythonTranscriptionPayload p) {
    TranscriptionResultResponse resultResponse = new TranscriptionResultResponse();
    resultResponse.setTranscriptionText(resolveTranscriptionText(p));
    resultResponse.setLanguage(blankToDefault(p.getLanguage(), "unknown"));
    resultResponse.setConfidence(p.getConfidenceScore());
    resultResponse.setDuration(p.getDuration());
    resultResponse.setModelUsed(blankToDefault(p.getModelUsed(), "unknown"));
    resultResponse.setProcessingTime(p.getProcessingTime());
    resultResponse.setCompletedAt(LocalDateTime.now());
    resultResponse.setStatus(TranscriptionStatus.COMPLETED);
    resultResponse.setSegments(toSegments(p));
    return resultResponse;
  }

  public JobSubmissionResponse toJobSubmissionResponse(PythonJobSubmitPayload payload) {
    JobSubmissionResponse r = new JobSubmissionResponse();
    if (payload == null) {
      r.setJobId(null);
      r.setStatus(null);
      r.setMessage("Job submitted");
      return r;
    }
    r.setJobId(String.valueOf(payload.getJobId()));
    r.setStatus(String.valueOf(payload.getStatus()));
    r.setMessage(payload.getMessage() != null ? payload.getMessage() : "Job submitted");
    return r;
  }

  public JobProgressResponse toJobProgressResponse(PythonJobProgressPayload progress) {
    JobProgressResponse progressResponse = new JobProgressResponse();
    progressResponse.setJobId(String.valueOf(progress.getJobId()));
    progressResponse.setStatus(String.valueOf(progress.getStatus()));
    progressResponse.setProgress(progress.getProgress());
    progressResponse.setMessage(progress.getMessage() != null ? progress.getMessage() : "");
    if (progress.getResult() != null) {
      PythonTranscriptionPayload resultPayload = progress.getResult();
      progressResponse.setResult(toTranscriptionApiResponse(resultPayload));
    }
    if (progress.getError() != null) {
      progressResponse.setError(String.valueOf(progress.getError()));
    }
    progressResponse.setCreatedAt(progress.getCreatedAt());
    progressResponse.setUpdatedAt(progress.getUpdatedAt());
    return progressResponse;
  }

  private static String blankToDefault(String v, String defaultValue) {
    if (v == null || v.isBlank()) {
      return defaultValue;
    }
    return v;
  }
}
