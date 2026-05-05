package com.shangmin.whisperrr.config;

import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Builds the JDBC URL from env vars matching {@code psql} workflows.
 *
 * <p><strong>Direct</strong> ({@code db.<ref>.supabase.co:5432}) is IPv6-first; some JVM / networks
 * see TLS EOF during connect while {@code psql} succeeds. Set {@code DATABASE_POOLER_REGION} (AWS
 * region from Supabase Connect → Session pooler, e.g. {@code ap-southeast-1}) to use the
 * IPv4-friendly shared pooler with username {@code postgres.<project-ref>}.
 */
@Configuration
@Profile("!test")
public class SupabasePostgresDataSourceConfig {

  @Bean
  DataSource dataSource(
      @Value("${DATABASE_PROJECT_REF}") String projectRef,
      @Value("${DATABASE_PASSWORD}") String password,
      @Value("${DATABASE_POOLER_REGION:}") String poolerRegion) {

    String ref = projectRef.trim();
    String region = poolerRegion.trim();

    HikariDataSource ds = new HikariDataSource();
    if (region.isEmpty()) {
      ds.setJdbcUrl("jdbc:postgresql://db." + ref + ".supabase.co:5432/postgres");
      ds.setUsername("postgres");
    } else {
      ds.setJdbcUrl("jdbc:postgresql://aws-0-" + region + ".pooler.supabase.com:5432/postgres");
      ds.setUsername("postgres." + ref);
    }
    ds.setPassword(password);
    ds.setMaximumPoolSize(10);
    ds.setMinimumIdle(2);
    ds.addDataSourceProperty("sslmode", "require");
    return ds;
  }
}
