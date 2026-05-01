package com.shangmin.whisperrr.config;

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Cross-Origin Resource Sharing (CORS) configuration for frontend communication.
 *
 * <p>This configuration class enables the Spring Boot backend to accept cross-origin requests from
 * the React frontend application. CORS is essential for modern web applications where the frontend
 * and backend are served from different origins (different protocols, domains, or ports).
 *
 * <h3>CORS Configuration Overview:</h3>
 *
 * <p>The Whisperrr application requires CORS configuration because:
 *
 * <ul>
 *   <li><strong>Frontend:</strong> React app running on http://localhost:3737 (development)
 *   <li><strong>Backend:</strong> Spring Boot API running on http://localhost:7331
 *   <li><strong>Production:</strong> Frontend and backend may be on different domains
 *   <li><strong>Tunnels:</strong> Supports dynamic tunnel URLs (Cloudflare, Tailscale) via wildcard
 *       pattern
 * </ul>
 *
 * <h3>Security Considerations:</h3>
 *
 * <ul>
 *   <li><strong>Allowed Origins:</strong> Configurable list of trusted frontend URLs
 *   <li><strong>Allowed Methods:</strong> Limited to necessary HTTP methods (GET, POST, etc.)
 *   <li><strong>Allowed Headers:</strong> Controlled set of permitted request headers
 *   <li><strong>Credentials:</strong> Configurable support for cookies and authentication
 *   <li><strong>Preflight Caching:</strong> Optimized preflight request handling
 * </ul>
 *
 * <h3>Configuration Sources:</h3>
 *
 * <p>CORS settings are externalized in application.properties for easy environment-specific
 * configuration without code changes:
 *
 * <ul>
 *   <li>cors.allowed-origins: Comma-separated list of allowed frontend URLs, or "*" for all origins
 *       (useful for dynamic tunnel URLs)
 *   <li>cors.allowed-methods: HTTP methods permitted for cross-origin requests
 *   <li>cors.allowed-headers: Headers that can be sent in cross-origin requests
 *   <li>cors.allow-credentials: Whether to include cookies/auth in requests
 * </ul>
 *
 * <h3>Development vs Production:</h3>
 *
 * <ul>
 *   <li><strong>Development:</strong> Permissive settings for local development
 *   <li><strong>Production:</strong> Restrictive settings with specific domain whitelist
 *   <li><strong>Testing:</strong> Configurable for different testing environments
 * </ul>
 *
 * <h3>Implementation details:</h3>
 *
 * <p>CORS for {@code /api/**} is applied only via {@link WebMvcConfigurer}. If Spring Security is
 * adopted later, configure CORS on the security filter chain and replace or reconcile this mapping
 * as needed.
 *
 * <h3>Preflight Request Handling:</h3>
 *
 * <p>This configuration sets max-age to 3600 seconds (1 hour) to cache preflight responses.
 *
 * @author shangmin
 * @version 1.0
 * @since 2024
 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurer
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

  @Value("${cors.allowed-origins}")
  private String allowedOrigins;

  @Value("${cors.allowed-methods}")
  private String allowedMethods;

  @Value("${cors.allowed-headers}")
  private String allowedHeaders;

  @Value("${cors.allow-credentials}")
  private boolean allowCredentials;

  /**
   * Configure CORS mappings for all API endpoints under {@code /api/**}.
   *
   * @param registry the CORS registry to configure mappings
   */
  @Override
  public void addCorsMappings(CorsRegistry registry) {
    var corsRegistration =
        registry
            .addMapping("/api/**")
            .allowedMethods(allowedMethods.split(","))
            .allowedHeaders(allowedHeaders.split(","))
            .allowCredentials(allowCredentials)
            .maxAge(3600);

    // Support wildcard pattern for dynamic tunnel URLs (e.g., Cloudflare, Tailscale)
    // Using allowedOriginPatterns instead of allowedOrigins to support credentials with wildcard
    if ("*".equals(allowedOrigins.trim())) {
      corsRegistration.allowedOriginPatterns("*");
    } else {
      String[] originArray = allowedOrigins.split(",");
      String[] trimmedOrigins = Arrays.stream(originArray).map(String::trim).toArray(String[]::new);
      corsRegistration.allowedOriginPatterns(trimmedOrigins);
    }
  }
}
