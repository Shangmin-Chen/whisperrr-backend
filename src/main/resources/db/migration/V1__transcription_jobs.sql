-- Transcription job state (Phase 2 persistence). user_id references Supabase auth.users.
CREATE TABLE public.transcription_jobs (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL REFERENCES auth.users (id) ON DELETE CASCADE,
  status TEXT NOT NULL
    CHECK (
      status IN (
        'PENDING',
        'PROCESSING',
        'COMPLETED',
        'FAILED',
        'CANCELLED'
      )
    ),
  progress DOUBLE PRECISION,
  message TEXT,
  error TEXT,
  result JSONB,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_transcription_jobs_user_created
  ON public.transcription_jobs (user_id, created_at DESC);

CREATE OR REPLACE FUNCTION public.transcription_jobs_set_updated_at()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
  NEW.updated_at := now();
  RETURN NEW;
END;
$$;

CREATE TRIGGER transcription_jobs_set_updated_at
  BEFORE UPDATE ON public.transcription_jobs
  FOR EACH ROW
  EXECUTE FUNCTION public.transcription_jobs_set_updated_at();

ALTER TABLE public.transcription_jobs ENABLE ROW LEVEL SECURITY;

CREATE POLICY user_owns_jobs ON public.transcription_jobs
  FOR ALL
  TO authenticated
  USING (auth.uid() = user_id)
  WITH CHECK (auth.uid() = user_id);

GRANT SELECT, INSERT, UPDATE, DELETE ON public.transcription_jobs TO authenticated;
