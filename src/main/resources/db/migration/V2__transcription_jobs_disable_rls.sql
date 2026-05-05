-- Spring uses the DB postgres role via the Supabase pooler (postgres.<project>). That session does
-- not carry Supabase JWT claims, so auth.uid() is unset and RLS policies defined FOR authenticated
-- cause INSERT to fail with "new row violates row-level security policy". Authorization is enforced
-- in the Spring API (trust boundary); PostgREST clients should not expose this table directly.
ALTER TABLE public.transcription_jobs DISABLE ROW LEVEL SECURITY;
