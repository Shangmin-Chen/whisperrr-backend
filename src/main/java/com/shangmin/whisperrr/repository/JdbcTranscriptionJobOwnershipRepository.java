package com.shangmin.whisperrr.repository;

import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;

public final class JdbcTranscriptionJobOwnershipRepository
    implements TranscriptionJobOwnershipRepository {

  private static final String INSERT_PENDING =
      """
      INSERT INTO public.transcription_jobs (id, user_id, status)
      VALUES (?, ?, 'PENDING')
      """;

  private static final String EXISTS_FOR_USER =
      """
      SELECT 1 FROM public.transcription_jobs
      WHERE id = ? AND user_id = ?
      LIMIT 1
      """;

  private final JdbcTemplate jdbcTemplate;

  public JdbcTranscriptionJobOwnershipRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public void recordJobSubmitted(UUID jobId, UUID userId) {
    jdbcTemplate.update(INSERT_PENDING, jobId, userId);
  }

  @Override
  public boolean isOwnedByUser(UUID jobId, UUID userId) {
    Boolean found =
        jdbcTemplate.query(
            EXISTS_FOR_USER, rs -> rs.next() ? Boolean.TRUE : Boolean.FALSE, jobId, userId);
    return Boolean.TRUE.equals(found);
  }
}
