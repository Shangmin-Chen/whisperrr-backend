package com.shangmin.whisperrr.whisperrr_api;

import com.shangmin.whisperrr.repository.TranscriptionJobOwnershipRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(
    properties = {
      "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://jwt-test.local/auth/v1/.well-known/jwks.json",
      "spring.security.oauth2.resourceserver.jwt.issuer-uri=https://jwt-test.local/auth/v1"
    })
class WhisperrrApiApplicationTests {

  /** Tests exclude JDBC; the API still wires beans that depend on this repository. */
  @MockBean private TranscriptionJobOwnershipRepository transcriptionJobOwnershipRepository;

  @Test
  void contextLoads() {}
}
