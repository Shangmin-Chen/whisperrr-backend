package com.shangmin.whisperrr.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

/**
 * Explicit JWT decoder for Supabase Auth access tokens (ES256, JWKS at {@code jwk-set-uri}).
 *
 * <p>Spring Security’s {@link NimbusJwtDecoder#withJwkSetUri(String)} defaults to <strong>RS256
 * only</strong>. Supabase access tokens are <strong>ES256</strong>; without {@link
 * SignatureAlgorithm#ES256} the decoder fails with “Another algorithm expected, or no matching
 * key(s) found”. RS256 remains enabled for integration tests that mint RSA JWTs against a local
 * JWKS server.
 */
@Configuration
@Profile("!test")
public class SupabaseJwtDecoderConfig {

  @Bean
  JwtDecoder jwtDecoder(
      @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}") String jwkSetUri,
      @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuerUri) {

    String jwk = sanitizeUri(jwkSetUri);
    NimbusJwtDecoder decoder =
        NimbusJwtDecoder.withJwkSetUri(jwk)
            .jwsAlgorithm(SignatureAlgorithm.ES256)
            .jwsAlgorithm(SignatureAlgorithm.RS256)
            .build();
    String issuer = sanitizeUri(issuerUri).replaceAll("/+$", "");
    decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer));
    return decoder;
  }

  private static String sanitizeUri(String raw) {
    if (raw == null) {
      return "";
    }
    return raw.trim().replace("\r", "").replace("\n", "");
  }
}
