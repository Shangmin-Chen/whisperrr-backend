package com.shangmin.whisperrr.config;

import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Builds the JDBC URL for Supabase Postgres via the shared <strong>session pooler</strong>
 * (IPv4-friendly).
 *
 * <p>Required env: {@code DATABASE_PROJECT_REF}, {@code DATABASE_PASSWORD}, {@code
 * DATABASE_POOLER_REGION}. The region slug comes from Supabase <strong>Project Settings → Connect →
 * Session pooler</strong> (host {@code aws-0-<region>.pooler.supabase.com}). JDBC URL is {@code
 * jdbc:postgresql://aws-0-<region>.pooler.supabase.com:5432/postgres} with username {@code
 * postgres.<project-ref>}.
 */
@Configuration
@Profile("!test")
public class SupabasePostgresDataSourceConfig {

  @Bean
  DataSource dataSource(
      @Value("${DATABASE_PROJECT_REF}") String projectRef,
      @Value("${DATABASE_PASSWORD}") String password,
      @Value("${DATABASE_POOLER_REGION}") String poolerRegion) {

    String ref = projectRef.trim();
    String region = poolerRegion.trim();
    if (region.isEmpty()) {
      throw new IllegalStateException(
          "DATABASE_POOLER_REGION must be non-empty (Supabase Connect → Session pooler region, e.g. ap-southeast-1)");
    }

    HikariDataSource ds = new HikariDataSource();
    ds.setJdbcUrl("jdbc:postgresql://aws-1-" + region + ".pooler.supabase.com:5432/postgres");
    ds.setUsername("postgres." + ref);
    ds.setPassword(password);
    ds.setMaximumPoolSize(10);
    ds.setMinimumIdle(2);
    ds.addDataSourceProperty("sslmode", "require");
    return ds;
  }
}
