package com.shangmin.whisperrr.config;

import com.shangmin.whisperrr.repository.JdbcTranscriptionJobOwnershipRepository;
import com.shangmin.whisperrr.repository.TranscriptionJobOwnershipRepository;
import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class TranscriptionJobRepositoryConfig {

  @Bean
  @ConditionalOnMissingBean(TranscriptionJobOwnershipRepository.class)
  TranscriptionJobOwnershipRepository transcriptionJobOwnershipRepository(DataSource dataSource) {
    return new JdbcTranscriptionJobOwnershipRepository(new JdbcTemplate(dataSource));
  }
}
