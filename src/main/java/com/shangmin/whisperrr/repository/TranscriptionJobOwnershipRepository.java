package com.shangmin.whisperrr.repository;

import java.util.UUID;

/**
 * Persists which Supabase user ({@code auth.users.id}) owns each async transcription {@code jobId}
 * so progress polling can be scoped server-side.
 */
public interface TranscriptionJobOwnershipRepository {

  /**
   * Records that {@code jobId} was submitted by {@code userId}. Inserts a minimal {@code PENDING}
   * row into {@code transcription_jobs}.
   */
  void recordJobSubmitted(UUID jobId, UUID userId);

  /** Returns true if a row exists for this job and user. */
  boolean isOwnedByUser(UUID jobId, UUID userId);
}
