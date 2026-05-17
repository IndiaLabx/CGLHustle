-- Local Snapshot Validation Boundary
ALTER TABLE public.question_snapshots
  ADD CONSTRAINT uq_question_snapshots_session_question UNIQUE (quiz_session_id, question_id);

-- Extra Replay Safety
ALTER TABLE public.quiz_sessions
  ADD CONSTRAINT uq_quiz_sessions_user_last_mutation UNIQUE (user_id, last_mutation_id);

ALTER TABLE public.user_answers
  ADD CONSTRAINT uq_user_answers_user_event UNIQUE (user_id, event_id);

-- Tenant-Isolated Idempotency Ledger
ALTER TABLE public.sync_events_ack
  ADD CONSTRAINT uq_sync_events_ack_user_idempotency UNIQUE (user_id, idempotency_key);

-- Integrity Checks
ALTER TABLE public.quiz_sessions
  ADD CONSTRAINT chk_quiz_sessions_active_duration CHECK (active_duration_ms >= 0),
  ADD CONSTRAINT chk_quiz_sessions_paused_duration CHECK (total_paused_duration_ms >= 0);

-- Performance & Lookup Indexes
CREATE INDEX idx_quiz_sessions_lookup ON public.quiz_sessions (user_id, status, updated_at DESC);
CREATE INDEX idx_user_answers_lookup ON public.user_answers (session_id, question_id);
CREATE INDEX idx_user_answers_sequence ON public.user_answers (session_id, question_id, attempt_sequence DESC);
