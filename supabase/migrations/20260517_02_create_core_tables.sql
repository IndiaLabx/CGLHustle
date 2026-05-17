CREATE TABLE public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    full_name TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE public.bookmarks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    question_id UUID NOT NULL,
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE public.saved_quizzes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    filters JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE public.quiz_sessions (
    session_id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    quiz_metadata_id UUID NOT NULL,
    status public.session_status NOT NULL DEFAULT 'NOT_STARTED',
    session_version INT NOT NULL DEFAULT 1,
    last_mutation_id TEXT NOT NULL,
    start_time TIMESTAMPTZ,
    last_paused_time TIMESTAMPTZ,
    end_time TIMESTAMPTZ,
    total_paused_duration_ms BIGINT NOT NULL DEFAULT 0,
    active_duration_ms BIGINT NOT NULL DEFAULT 0,
    client_generated_at TIMESTAMPTZ NOT NULL,
    server_received_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE public.question_snapshots (
    snapshot_id TEXT PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    quiz_session_id UUID NOT NULL REFERENCES public.quiz_sessions(session_id) ON DELETE CASCADE,
    question_id UUID NOT NULL,
    content_version INT NOT NULL DEFAULT 1,
    snapshot_hash TEXT NOT NULL,
    source_project TEXT NOT NULL,
    source_fetched_at BIGINT NOT NULL,
    is_deleted_upstream BOOLEAN DEFAULT FALSE,
    question_text TEXT NOT NULL,
    options JSONB NOT NULL,
    correct_answer TEXT NOT NULL,
    explanation TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE public.user_answers (
    event_id TEXT PRIMARY KEY,
    session_id UUID NOT NULL REFERENCES public.quiz_sessions(session_id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    question_id UUID NOT NULL,
    mutation_type public.answer_mutation_type NOT NULL,
    selected_option TEXT,
    attempt_sequence INT NOT NULL,
    client_generated_at TIMESTAMPTZ NOT NULL,
    server_received_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE public.sync_events_ack (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    idempotency_key TEXT NOT NULL,
    status TEXT NOT NULL,
    request_hash TEXT,
    response_jsonb JSONB,
    processed_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE public.sync_conflicts_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    table_name TEXT NOT NULL,
    event_id TEXT NOT NULL,
    payload JSONB,
    conflict_reason TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
