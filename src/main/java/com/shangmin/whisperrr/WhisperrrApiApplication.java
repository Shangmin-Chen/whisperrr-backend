package com.shangmin.whisperrr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot entry point for the Whisperrr HTTP API: upload validation, Supabase JWT security for
 * {@code /api/**}, and proxying to the Python transcription service (synchronous transcribe and
 * async jobs with progress polling).
 *
 * <p><strong>Persistence:</strong> A JDBC {@link javax.sql.DataSource} and Flyway are registered
 * from {@code DATABASE_PROJECT_REF} and {@code DATABASE_PASSWORD} in {@link
 * com.shangmin.whisperrr.config.SupabasePostgresDataSourceConfig} (direct host or optional {@code
 * DATABASE_POOLER_REGION}; see README). Those env vars must resolve at runtime (e.g. from {@code
 * .env}) or the process will not start. Flyway applies scripts under {@code classpath:db/migration}
 * during startup before the application serves traffic. Tests activate the {@code test} profile and
 * skip that configuration.
 *
 * <p>Shared {@code application.properties} holds CORS, multipart limits, Python service URLs, and
 * logging; JWT and datasource settings live in {@code application.yml}.
 *
 * @author shangmin
 * @version 2.0
 * @since 2024
 * @see com.shangmin.whisperrr.controller.AudioController
 * @see com.shangmin.whisperrr.service.AudioService
 * @see com.shangmin.whisperrr.config.CorsConfig
 */
@SpringBootApplication
public class WhisperrrApiApplication {

  /**
   * Starts the Spring context, embedded web server (default port 7331), and Flyway when a JDBC
   * datasource is configured.
   *
   * @param args e.g. {@code --server.port=7331}, {@code --spring.profiles.active=prod}, {@code
   *     --logging.level.com.shangmin.whisperrr=DEBUG}
   */
  public static void main(String[] args) {
    SpringApplication.run(WhisperrrApiApplication.class, args);
  }
}
