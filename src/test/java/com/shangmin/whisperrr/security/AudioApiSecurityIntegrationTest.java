package com.shangmin.whisperrr.security;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.shangmin.whisperrr.dto.JobProgressResponse;
import com.shangmin.whisperrr.repository.TranscriptionJobOwnershipRepository;
import com.shangmin.whisperrr.service.AudioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
    properties = {
      "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://jwt-test.local/auth/v1/.well-known/jwks.json",
      "spring.security.oauth2.resourceserver.jwt.issuer-uri=https://jwt-test.local/auth/v1",
      "whisperrr.service.url=http://127.0.0.1:59999"
    })
@AutoConfigureMockMvc
class AudioApiSecurityIntegrationTest {

  private static final String ISSUER = "https://jwt-test.local/auth/v1";

  @Autowired private MockMvc mockMvc;

  @MockBean private TranscriptionJobOwnershipRepository transcriptionJobOwnershipRepository;

  @MockBean private AudioService audioService;

  @Test
  void api_withoutBearerToken_returns401() throws Exception {
    mockMvc.perform(get("/api/audio/jobs/job-1/progress")).andExpect(status().isUnauthorized());
  }

  @Test
  void actuatorHealth_withoutToken_returns200() throws Exception {
    mockMvc.perform(get("/actuator/health")).andExpect(status().isOk());
  }

  @Test
  void api_withJwt_matchingIssuer_returns200() throws Exception {
    when(audioService.getJobProgress(anyString(), eq("job-1")))
        .thenReturn(new JobProgressResponse());

    mockMvc
        .perform(
            get("/api/audio/jobs/job-1/progress")
                .with(jwt().jwt(j -> j.subject("test-uuid").issuer(ISSUER))))
        .andExpect(status().isOk());
  }
}
