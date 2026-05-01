package com.shangmin.whisperrr.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shangmin.whisperrr.client.PythonTranscriptionClient;
import com.shangmin.whisperrr.dto.TranscriptionResultResponse;
import com.shangmin.whisperrr.dto.python.PythonJobSubmitPayload;
import com.shangmin.whisperrr.dto.python.PythonTranscriptionPayload;
import com.shangmin.whisperrr.exception.FileValidationException;
import com.shangmin.whisperrr.exception.TranscriptionProcessingException;
import com.shangmin.whisperrr.service.impl.AudioServiceImpl;
import com.shangmin.whisperrr.service.support.PythonTranscriptionResponseMapper;
import java.io.IOException;
import java.io.UncheckedIOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.multipart.MultipartFile;

/**
 * Unit tests for AudioServiceImpl covering validation, delegation to {@link
 * PythonTranscriptionClient}, and error handling.
 */
@ExtendWith(MockitoExtension.class)
class AudioServiceImplTest {

  @Mock(lenient = true)
  private PythonTranscriptionClient pythonClient;

  private PythonTranscriptionResponseMapper transcriptionMapper;
  private AudioServiceImpl audioService;

  @Mock private MultipartFile mockFile;

  @BeforeEach
  void setUp() {
    transcriptionMapper = new PythonTranscriptionResponseMapper();
    audioService = new AudioServiceImpl(pythonClient, transcriptionMapper);
  }

  @Test
  void testTranscribeAudio_WithNullFile_ThrowsException() {
    assertThrows(
        FileValidationException.class, () -> audioService.transcribeAudio(null, null, null, null));
  }

  @Test
  void testTranscribeAudio_WithEmptyFile_ThrowsException() {
    when(mockFile.isEmpty()).thenReturn(true);

    assertThrows(
        FileValidationException.class,
        () -> audioService.transcribeAudio(mockFile, null, null, null));
  }

  @Test
  void testTranscribeAudio_WithFileTooLarge_ThrowsException() {
    when(mockFile.isEmpty()).thenReturn(false);
    when(mockFile.getSize()).thenReturn(60L * 1024 * 1024);

    assertThrows(
        FileValidationException.class,
        () -> audioService.transcribeAudio(mockFile, null, null, null));
  }

  @Test
  void testTranscribeAudio_WithNullFilename_ThrowsException() {
    when(mockFile.isEmpty()).thenReturn(false);
    when(mockFile.getSize()).thenReturn(1024L);
    when(mockFile.getOriginalFilename()).thenReturn(null);

    assertThrows(
        FileValidationException.class,
        () -> audioService.transcribeAudio(mockFile, null, null, null));
  }

  @Test
  void testTranscribeAudio_WithUnsupportedExtension_ThrowsException() {
    lenient().when(mockFile.isEmpty()).thenReturn(false);
    lenient().when(mockFile.getSize()).thenReturn(1024L);
    lenient().when(mockFile.getOriginalFilename()).thenReturn("file.txt");
    lenient().when(mockFile.getContentType()).thenReturn("text/plain");

    assertThrows(
        FileValidationException.class,
        () -> audioService.transcribeAudio(mockFile, null, null, null));
  }

  @Test
  void testTranscribeAudio_WithInvalidContentType_ThrowsException() {
    when(mockFile.isEmpty()).thenReturn(false);
    when(mockFile.getSize()).thenReturn(1024L);
    when(mockFile.getOriginalFilename()).thenReturn("audio.mp3");
    when(mockFile.getContentType()).thenReturn("application/json");

    assertThrows(
        FileValidationException.class,
        () -> audioService.transcribeAudio(mockFile, null, null, null));
  }

  @Test
  void testTranscribeAudio_WithNoExtension_ThrowsException() {
    lenient().when(mockFile.isEmpty()).thenReturn(false);
    lenient().when(mockFile.getSize()).thenReturn(1024L);
    lenient().when(mockFile.getOriginalFilename()).thenReturn("audio");
    lenient().when(mockFile.getContentType()).thenReturn("audio/mpeg");

    assertThrows(
        FileValidationException.class,
        () -> audioService.transcribeAudio(mockFile, null, null, null));
  }

  @Test
  void testTranscribeAudio_WhenPythonServiceUnavailable_ThrowsException() {
    setupValidFile();

    when(pythonClient.postTranscribe(
            any(), nullable(String.class), nullable(String.class), nullable(String.class)))
        .thenThrow(new ResourceAccessException("Connection refused"));

    assertThrows(
        TranscriptionProcessingException.class,
        () -> audioService.transcribeAudio(mockFile, null, null, null));
  }

  @Test
  void testTranscribeAudio_WhenPythonServiceTimeout_ThrowsException() {
    setupValidFile();

    when(pythonClient.postTranscribe(
            any(), nullable(String.class), nullable(String.class), nullable(String.class)))
        .thenThrow(new ResourceAccessException("Read timed out"));

    assertThrows(
        TranscriptionProcessingException.class,
        () -> audioService.transcribeAudio(mockFile, null, null, null));
  }

  @Test
  void testTranscribeAudio_WhenPythonServiceReturns400_ThrowsException() {
    setupValidFile();

    HttpClientErrorException exception =
        new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Invalid request");

    when(pythonClient.postTranscribe(
            any(), nullable(String.class), nullable(String.class), nullable(String.class)))
        .thenThrow(exception);

    assertThrows(
        TranscriptionProcessingException.class,
        () -> audioService.transcribeAudio(mockFile, null, null, null));
  }

  @Test
  void testTranscribeAudio_WhenPythonServiceReturns413_ThrowsException() {
    setupValidFile();

    HttpClientErrorException exception =
        new HttpClientErrorException(HttpStatus.PAYLOAD_TOO_LARGE, "File too large");

    when(pythonClient.postTranscribe(
            any(), nullable(String.class), nullable(String.class), nullable(String.class)))
        .thenThrow(exception);

    assertThrows(
        TranscriptionProcessingException.class,
        () -> audioService.transcribeAudio(mockFile, null, null, null));
  }

  @Test
  void testTranscribeAudio_WhenPythonServiceReturns500_ThrowsException() {
    setupValidFile();

    HttpServerErrorException exception =
        new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");

    when(pythonClient.postTranscribe(
            any(), nullable(String.class), nullable(String.class), nullable(String.class)))
        .thenThrow(exception);

    assertThrows(
        TranscriptionProcessingException.class,
        () -> audioService.transcribeAudio(mockFile, null, null, null));
  }

  @Test
  void testTranscribeAudio_WhenPythonServiceReturns503_ThrowsException() {
    setupValidFile();

    HttpServerErrorException exception =
        new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE, "Service unavailable");

    when(pythonClient.postTranscribe(
            any(), nullable(String.class), nullable(String.class), nullable(String.class)))
        .thenThrow(exception);

    assertThrows(
        TranscriptionProcessingException.class,
        () -> audioService.transcribeAudio(mockFile, null, null, null));
  }

  @Test
  void testTranscribeAudio_WhenResponseBodyIsNull_ThrowsException() {
    setupValidFile();

    ResponseEntity<PythonTranscriptionPayload> response = ResponseEntity.ok(null);

    when(pythonClient.postTranscribe(
            any(), nullable(String.class), nullable(String.class), nullable(String.class)))
        .thenReturn(response);

    assertThrows(
        TranscriptionProcessingException.class,
        () -> audioService.transcribeAudio(mockFile, null, null, null));
  }

  @Test
  void testTranscribeAudio_WhenResponseHasEmptyText_ThrowsException() {
    setupValidFile();

    PythonTranscriptionPayload body = new PythonTranscriptionPayload();
    body.setText("");
    body.setLanguage("en");

    ResponseEntity<PythonTranscriptionPayload> response = ResponseEntity.ok(body);

    when(pythonClient.postTranscribe(
            any(), nullable(String.class), nullable(String.class), nullable(String.class)))
        .thenReturn(response);

    assertThrows(
        TranscriptionProcessingException.class,
        () -> audioService.transcribeAudio(mockFile, null, null, null));
  }

  @Test
  void testTranscribeAudio_WhenResponseMissingText_ThrowsException() {
    setupValidFile();

    PythonTranscriptionPayload body = new PythonTranscriptionPayload();
    body.setLanguage("en");

    ResponseEntity<PythonTranscriptionPayload> response = ResponseEntity.ok(body);

    when(pythonClient.postTranscribe(
            any(), nullable(String.class), nullable(String.class), nullable(String.class)))
        .thenReturn(response);

    assertThrows(
        TranscriptionProcessingException.class,
        () -> audioService.transcribeAudio(mockFile, null, null, null));
  }

  @Test
  void testTranscribeAudio_WhenResponseHasNullText_ThrowsException() {
    setupValidFile();

    PythonTranscriptionPayload body = new PythonTranscriptionPayload();
    body.setText(null);
    body.setLanguage("en");

    ResponseEntity<PythonTranscriptionPayload> response = ResponseEntity.ok(body);

    when(pythonClient.postTranscribe(
            any(), nullable(String.class), nullable(String.class), nullable(String.class)))
        .thenReturn(response);

    assertThrows(
        TranscriptionProcessingException.class,
        () -> audioService.transcribeAudio(mockFile, null, null, null));
  }

  @Test
  void testTranscribeAudio_WhenConfidenceInvalidAfterDeserialization_ReturnsNullConfidence() {
    setupValidFile();

    PythonTranscriptionPayload body = new PythonTranscriptionPayload();
    body.setText("Transcribed text");
    body.setLanguage("en");
    body.setConfidenceScore(null);

    ResponseEntity<PythonTranscriptionPayload> response = ResponseEntity.ok(body);

    when(pythonClient.postTranscribe(
            any(), nullable(String.class), nullable(String.class), nullable(String.class)))
        .thenReturn(response);

    TranscriptionResultResponse result = audioService.transcribeAudio(mockFile, null, null, null);
    assertNotNull(result);
    assertNull(result.getConfidence());
  }

  @Test
  void testTranscribeAudio_WhenDurationIsInvalidType_HandlesGracefully() {
    setupValidFile();

    PythonTranscriptionPayload body = new PythonTranscriptionPayload();
    body.setText("Transcribed text");
    body.setLanguage("en");
    body.setDuration(null);

    ResponseEntity<PythonTranscriptionPayload> response = ResponseEntity.ok(body);

    when(pythonClient.postTranscribe(
            any(), nullable(String.class), nullable(String.class), nullable(String.class)))
        .thenReturn(response);

    assertDoesNotThrow(
        () -> {
          TranscriptionResultResponse result =
              audioService.transcribeAudio(mockFile, null, null, null);
          assertNotNull(result);
        });
  }

  @Test
  void testTranscribeAudio_WhenUpstreamReadFails_ThrowsException() {
    setupValidFile();

    when(pythonClient.postTranscribe(
            any(), nullable(String.class), nullable(String.class), nullable(String.class)))
        .thenThrow(new UncheckedIOException(new IOException("Failed to read file")));

    assertThrows(
        TranscriptionProcessingException.class,
        () -> audioService.transcribeAudio(mockFile, null, null, null));
  }

  @Test
  void testTranscribeAudio_WithValidResponse_ReturnsResult() {
    setupValidFile();

    PythonTranscriptionPayload body = new PythonTranscriptionPayload();
    body.setText("Hello world");
    body.setLanguage("en");
    body.setConfidenceScore(0.95);
    body.setDuration(5.5);
    body.setModelUsed("base");
    body.setProcessingTime(2.3);

    ResponseEntity<PythonTranscriptionPayload> response = ResponseEntity.ok(body);

    when(pythonClient.postTranscribe(
            any(), nullable(String.class), nullable(String.class), nullable(String.class)))
        .thenReturn(response);

    TranscriptionResultResponse result = audioService.transcribeAudio(mockFile, null, null, null);

    assertNotNull(result);
    assertEquals("Hello world", result.getTranscriptionText());
    assertEquals("en", result.getLanguage());
    assertEquals(0.95, result.getConfidence());
    assertEquals(5.5, result.getDuration());
    assertEquals("base", result.getModelUsed());
  }

  @Test
  void testTranscribeAudio_WithModelSize_PassesToClient() {
    setupValidFile();

    PythonTranscriptionPayload body = new PythonTranscriptionPayload();
    body.setText("Test");
    body.setLanguage("en");

    ResponseEntity<PythonTranscriptionPayload> response = ResponseEntity.ok(body);

    when(pythonClient.postTranscribe(any(), eq("large"), isNull(), isNull())).thenReturn(response);

    audioService.transcribeAudio(mockFile, "large", null, null);

    verify(pythonClient).postTranscribe(mockFile, "large", null, null);
  }

  @Test
  void testSubmitTranscriptionJob_DelegatesToClient() {
    setupValidFile();
    PythonJobSubmitPayload submitted = new PythonJobSubmitPayload();
    submitted.setJobId("j1");
    submitted.setStatus("queued");
    submitted.setMessage("ok");
    ResponseEntity<PythonJobSubmitPayload> response = ResponseEntity.ok(submitted);

    when(pythonClient.postSubmitJob(
            any(), nullable(String.class), nullable(String.class), nullable(String.class)))
        .thenReturn(response);

    var jobResponse = audioService.submitTranscriptionJob(mockFile, null, null, null);
    assertEquals("j1", jobResponse.getJobId());
    assertEquals("queued", jobResponse.getStatus());
    verify(pythonClient)
        .postSubmitJob(
            any(), nullable(String.class), nullable(String.class), nullable(String.class));
  }

  private void setupValidFile() {
    when(mockFile.isEmpty()).thenReturn(false);
    when(mockFile.getSize()).thenReturn(1024L);
    when(mockFile.getOriginalFilename()).thenReturn("audio.mp3");
    when(mockFile.getContentType()).thenReturn("audio/mpeg");
  }
}
