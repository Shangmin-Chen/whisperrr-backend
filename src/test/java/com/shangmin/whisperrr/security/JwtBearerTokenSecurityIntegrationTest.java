package com.shangmin.whisperrr.security;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.shangmin.whisperrr.dto.JobProgressResponse;
import com.shangmin.whisperrr.repository.TranscriptionJobOwnershipRepository;
import com.shangmin.whisperrr.service.AudioService;
import com.sun.net.httpserver.HttpServer;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Issuer validation is exercised with a real Bearer token (signature verified via in-process JWKS
 * server). {@link
 * org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors#jwt}
 * skips JWT decoding and cannot test wrong {@code iss}.
 */
@SpringBootTest
@AutoConfigureMockMvc
class JwtBearerTokenSecurityIntegrationTest {

  private static final String EXPECTED_ISSUER = "https://jwt-test.local/auth/v1";

  private static HttpServer jwksServer;
  private static com.nimbusds.jose.jwk.RSAKey rsaKey;
  private static String jwksBody;

  static {
    try {
      rsaKey = new RSAKeyGenerator(2048).keyID("test-key").generate();
      jwksBody = new JWKSet(rsaKey.toPublicJWK()).toJSONObject(false).toString();
      jwksServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
      jwksServer.createContext(
          "/auth/v1/.well-known/jwks.json",
          exchange -> {
            byte[] body = jwksBody.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            OutputStream os = exchange.getResponseBody();
            os.write(body);
            os.close();
          });
      jwksServer.start();
    } catch (Exception e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  @DynamicPropertySource
  static void oauthProps(DynamicPropertyRegistry r) {
    int port = jwksServer.getAddress().getPort();
    r.add(
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri",
        () -> "http://127.0.0.1:" + port + "/auth/v1/.well-known/jwks.json");
    r.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> EXPECTED_ISSUER);
    r.add("whisperrr.service.url", () -> "http://127.0.0.1:59999");
  }

  @AfterAll
  static void tearDown() {
    if (jwksServer != null) {
      jwksServer.stop(0);
    }
  }

  @Autowired private MockMvc mockMvc;

  @MockBean private TranscriptionJobOwnershipRepository transcriptionJobOwnershipRepository;

  @MockBean private AudioService audioService;

  @Test
  void jobProgress_withBearerTokenWrongIssuer_returns401() throws Exception {
    String token = mintSignedJwt("https://wrong-issuer.example/auth/v1");

    mockMvc
        .perform(get("/api/audio/jobs/job-1/progress").header("Authorization", "Bearer " + token))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void jobProgress_withBearerTokenMatchingIssuer_returns200() throws Exception {
    when(audioService.getJobProgress(anyString(), eq("job-1")))
        .thenReturn(new JobProgressResponse());

    String token = mintSignedJwt(EXPECTED_ISSUER);

    mockMvc
        .perform(get("/api/audio/jobs/job-1/progress").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk());
  }

  private static String mintSignedJwt(String issuer) throws Exception {
    JWTClaimsSet claims =
        new JWTClaimsSet.Builder()
            .subject("test-uuid")
            .issuer(issuer)
            .issueTime(Date.from(Instant.now()))
            .expirationTime(Date.from(Instant.now().plusSeconds(300)))
            .build();
    SignedJWT jwt =
        new SignedJWT(
            new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaKey.getKeyID()).build(), claims);
    jwt.sign(new RSASSASigner(rsaKey.toRSAPrivateKey()));
    return jwt.serialize();
  }
}
