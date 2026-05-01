package com.shangmin.whisperrr.client;

import com.shangmin.whisperrr.dto.python.PythonJobProgressPayload;
import com.shangmin.whisperrr.dto.python.PythonJobSubmitPayload;
import com.shangmin.whisperrr.dto.python.PythonTranscriptionPayload;
import com.shangmin.whisperrr.util.SecurityUtils;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriBuilder;

/** HTTP client for the Python transcription service: multipart uploads and job polling. */
@Component
public class PythonTranscriptionClient {

  private final RestClient restClient;

  public PythonTranscriptionClient(
      @Qualifier("pythonTranscriptionRestClient") RestClient restClient) {
    this.restClient = restClient;
  }

  public ResponseEntity<PythonTranscriptionPayload> postTranscribe(
      MultipartFile audioFile, String modelSize, String language, String task) {
    try {
      MultiValueMap<String, HttpEntity<?>> multipart = buildMultipartBody(audioFile);
      return restClient
          .post()
          .uri(uriBuilder -> transcribeUri(uriBuilder, modelSize, language, task))
          .body(multipart)
          .retrieve()
          .toEntity(PythonTranscriptionPayload.class);
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to read uploaded audio", e);
    }
  }

  public ResponseEntity<PythonJobSubmitPayload> postSubmitJob(
      MultipartFile audioFile, String modelSize, String language, String task) {
    try {
      MultiValueMap<String, HttpEntity<?>> multipart = buildMultipartBody(audioFile);
      return restClient
          .post()
          .uri(uriBuilder -> submitJobUri(uriBuilder, modelSize, language, task))
          .body(multipart)
          .retrieve()
          .toEntity(PythonJobSubmitPayload.class);
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to read uploaded audio", e);
    }
  }

  public ResponseEntity<PythonJobProgressPayload> getJobProgress(String jobId) {
    return restClient
        .get()
        .uri("/jobs/{jobId}/progress", jobId)
        .retrieve()
        .toEntity(PythonJobProgressPayload.class);
  }

  private static URI transcribeUri(
      UriBuilder uriBuilder, String modelSize, String language, String task) {
    UriBuilder b = uriBuilder.path("/transcribe");
    appendTranscriptionQueryParams(b, modelSize, language, task);
    return b.build();
  }

  private static URI submitJobUri(
      UriBuilder uriBuilder, String modelSize, String language, String task) {
    UriBuilder b = uriBuilder.path("/jobs/submit");
    appendTranscriptionQueryParams(b, modelSize, language, task);
    return b.build();
  }

  private static void appendTranscriptionQueryParams(
      UriBuilder b, String modelSize, String language, String task) {
    if (modelSize != null && !modelSize.trim().isEmpty()) {
      b.queryParam("model_size", modelSize.trim());
    }
    if (language != null && !language.trim().isEmpty()) {
      b.queryParam("language", language.trim());
    }
    if (task != null && !task.trim().isEmpty()) {
      b.queryParam("task", task.trim());
    }
  }

  private static MultiValueMap<String, HttpEntity<?>> buildMultipartBody(MultipartFile audioFile)
      throws IOException {
    String filename = SecurityUtils.sanitizeFilename(audioFile.getOriginalFilename());
    if (filename == null) {
      filename = "audio_file." + SecurityUtils.getFileExtension(audioFile.getOriginalFilename());
    }
    byte[] fileBytes = audioFile.getBytes();

    MultipartBodyBuilder builder = new MultipartBodyBuilder();
    builder.part("file", new ByteArrayResource(fileBytes)).filename(filename);
    return builder.build();
  }
}
