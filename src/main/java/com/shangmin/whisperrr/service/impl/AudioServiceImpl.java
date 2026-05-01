package com.shangmin.whisperrr.service.impl;

import com.shangmin.whisperrr.client.PythonTranscriptionClient;
import com.shangmin.whisperrr.config.AppConfig;
import com.shangmin.whisperrr.dto.JobProgressResponse;
import com.shangmin.whisperrr.dto.JobSubmissionResponse;
import com.shangmin.whisperrr.dto.TranscriptionResultResponse;
import com.shangmin.whisperrr.dto.python.PythonJobSubmitPayload;
import com.shangmin.whisperrr.dto.python.PythonTranscriptionPayload;
import com.shangmin.whisperrr.enums.AudioFormat;
import com.shangmin.whisperrr.exception.FileValidationException;
import com.shangmin.whisperrr.exception.TranscriptionProcessingException;
import com.shangmin.whisperrr.service.AudioService;
import com.shangmin.whisperrr.service.support.PythonTranscriptionResponseMapper;
import com.shangmin.whisperrr.util.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service implementation for audio transcription operations.
 *
 * <p>Validates uploads and delegates HTTP to {@link PythonTranscriptionClient}; maps responses via
 * {@link PythonTranscriptionResponseMapper}.
 */
@Service
public class AudioServiceImpl implements AudioService {

  private static final Logger logger = LoggerFactory.getLogger(AudioServiceImpl.class);

  private final PythonTranscriptionClient pythonClient;
  private final PythonTranscriptionResponseMapper transcriptionMapper;

  public AudioServiceImpl(
      PythonTranscriptionClient pythonClient,
      PythonTranscriptionResponseMapper transcriptionMapper) {
    this.pythonClient = pythonClient;
    this.transcriptionMapper = transcriptionMapper;
  }

  @Override
  public TranscriptionResultResponse transcribeAudio(
      MultipartFile audioFile, String modelSize, String language, String task) {
    validateAudioFile(audioFile);

    try {
      PythonTranscriptionPayload payload =
          requireOkBody(pythonClient.postTranscribe(audioFile, modelSize, language, task));

      String transcriptionText = transcriptionMapper.resolveTranscriptionText(payload);
      if (transcriptionText.isEmpty()) {
        throw new TranscriptionProcessingException(
            "Python service returned empty transcription result");
      }

      return transcriptionMapper.toTranscriptionApiResponse(payload);

    } catch (TranscriptionProcessingException e) {
      throw e;
    } catch (Exception e) {
      throw mapPythonFailure("Transcription", e);
    }
  }

  @Override
  public void validateAudioFile(MultipartFile audioFile) {
    if (audioFile == null || audioFile.isEmpty()) {
      throw new FileValidationException("Audio file is required");
    }

    if (audioFile.getSize() > AppConfig.MAX_FILE_SIZE_BYTES) {
      throw new FileValidationException(
          "File size exceeds maximum allowed size of " + AppConfig.MAX_FILE_SIZE_MB + "MB");
    }

    String originalFilename = audioFile.getOriginalFilename();
    if (originalFilename == null) {
      throw new FileValidationException("File must have a valid name");
    }

    String extension = SecurityUtils.getFileExtension(originalFilename).toLowerCase();
    AudioFormat format = AudioFormat.fromExtension(extension);
    if (format == null) {
      throw new FileValidationException(
          "Unsupported file type. Supported types: " + AudioFormat.supportedExtensionsListing());
    }

    String contentType = audioFile.getContentType();
    if (contentType == null || contentType.isBlank()) {
      throw new FileValidationException("File must declare an audio or video Content-Type.");
    }
    if (!format.isCompatibleContentType(contentType)) {
      throw new FileValidationException(
          "Content-Type is not compatible with ." + extension + " uploads.");
    }
  }

  @Override
  public JobSubmissionResponse submitTranscriptionJob(
      MultipartFile audioFile, String modelSize, String language, String task) {
    validateAudioFile(audioFile);

    try {
      PythonJobSubmitPayload body =
          requireOkBody(pythonClient.postSubmitJob(audioFile, modelSize, language, task));
      return transcriptionMapper.toJobSubmissionResponse(body);

    } catch (TranscriptionProcessingException e) {
      throw e;
    } catch (Exception e) {
      throw mapPythonFailure("Job submission", e);
    }
  }

  @Override
  public JobProgressResponse getJobProgress(String jobId) {
    try {
      var response = pythonClient.getJobProgress(jobId);
      return transcriptionMapper.toJobProgressResponse(requireOkBody(response));

    } catch (HttpClientErrorException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        throw new TranscriptionProcessingException("Job not found: " + jobId);
      }
      throw mapStatusCodeFailure(e);
    } catch (ResourceAccessException e) {
      logger.error("Failed to connect to Python service: {}", e.getMessage(), e);
      throw new TranscriptionProcessingException("Transcription service is unavailable", e);
    } catch (Exception e) {
      logger.error("Failed to get job progress: {}", e.getMessage(), e);
      throw new TranscriptionProcessingException(
          "Failed to get job progress: " + e.getMessage(), e);
    }
  }

  private static <T> T requireOkBody(ResponseEntity<T> response) {
    if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
      return response.getBody();
    }
    throw new TranscriptionProcessingException(
        "Python service returned unexpected response: "
            + (response.getStatusCode() != null ? response.getStatusCode() : "null status"));
  }

  private TranscriptionProcessingException mapPythonFailure(String operation, Exception e) {
    if (e instanceof ResourceAccessException ex) {
      logger.error("Failed to connect to Python service: {}", ex.getMessage(), ex);
      return new TranscriptionProcessingException("Transcription service is unavailable", ex);
    }
    if (e instanceof HttpStatusCodeException ex) {
      return mapStatusCodeFailure(ex);
    }
    logger.error("{} failed: {}", operation, e.getMessage(), e);
    return new TranscriptionProcessingException(operation + " failed: " + e.getMessage(), e);
  }

  private TranscriptionProcessingException mapStatusCodeFailure(HttpStatusCodeException e) {
    String responseBody = e.getResponseBodyAsString();
    logger.error(
        "Python service error ({}): {}. Response: {}",
        e.getStatusCode(),
        e.getMessage(),
        responseBody,
        e);
    return new TranscriptionProcessingException(
        "Transcription service error: " + e.getStatusCode(), e);
  }
}
