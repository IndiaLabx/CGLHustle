-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. PROFILES
CREATE TABLE public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    full_name TEXT,
    avatar_url TEXT,
    target_exam TEXT,
    subscription_status TEXT DEFAULT 'free',
    is_shadow_flagged BOOLEAN DEFAULT FALSE
);

-- 2. SAVED QUIZZES / BOOKMARKS
-- Users can save specific quizzes or bookmark individual questions for later review.
-- Note: question_id references GK LLM, so no foreign key constraint can be placed here.
CREATE TABLE public.bookmarks (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    question_id UUID NOT NULL, -- Logical ref to GK LLM
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    notes TEXT,
    UNIQUE(user_id, question_id)
);

CREATE TABLE public.saved_quizzes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    filters JSONB, -- The criteria used to generate this quiz (e.g., subject, difficulty)
    is_downloaded_locally BOOLEAN DEFAULT FALSE
);

-- 3. QUIZ SESSIONS
-- Adhering to the hardened contract with terminal safety and versioning.
CREATE TYPE session_status AS ENUM (
    'NOT_STARTED',
    'IN_PROGRESS',
    'PAUSED',
    'SUBMITTED_LOCAL',
    'SYNCED_FINAL',
    'TERMINATED_CONFLICT',
    'ABANDONED'
);

CREATE TABLE public.quiz_sessions (
    session_id UUID PRIMARY KEY, -- Driven by client UUID generation
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    quiz_metadata_id UUID NOT NULL, -- Logical ref to the generated quiz config
    status session_status NOT NULL DEFAULT 'NOT_STARTED',

    -- Timer & State Tracking
    start_time TIMESTAMPTZ,
    last_paused_time TIMESTAMPTZ,
    end_time TIMESTAMPTZ,
    total_paused_duration_ms BIGINT DEFAULT 0,
    active_duration_ms BIGINT DEFAULT 0, -- Incremental active time
    current_question_id UUID, -- Logical ref

    -- Sync & Versioning (Hardened Contracts)
    session_version INT NOT NULL DEFAULT 1,
    last_mutation_id UUID NOT NULL,
    idempotency_key TEXT UNIQUE NOT NULL, -- format: userId:sessionId:sessionVersion:eventId

    -- Device Info & Security
    device_fingerprint TEXT,
    client_generated_at TIMESTAMPTZ NOT NULL,
    server_received_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 4. USER ANSWERS
-- Adhering to the append-only mutation log philosophy
CREATE TYPE answer_mutation_type AS ENUM (
    'SELECT',
    'CLEAR',
    'MARK_REVIEW',
    'UNMARK_REVIEW'
);

CREATE TABLE public.user_answers (
    event_id UUID PRIMARY KEY, -- The mutation event ID (ULID string stored as UUID or text? We'll use UUID for consistency)
    session_id UUID NOT NULL REFERENCES public.quiz_sessions(session_id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    question_id UUID NOT NULL, -- Logical ref to GK LLM

    -- Mutation Data
    supersedes_event_id UUID REFERENCES public.user_answers(event_id) ON DELETE SET NULL,
    mutation_type answer_mutation_type NOT NULL,
    selected_option TEXT,
    is_correct BOOLEAN,
    time_taken_seconds DOUBLE PRECISION,

    -- Sync & Conflict Resolution
    attempt_sequence INT NOT NULL,
    idempotency_key TEXT UNIQUE NOT NULL, -- format: userId:sessionId:questionId:attemptSequence:eventId

    client_generated_at TIMESTAMPTZ NOT NULL,
    server_received_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Index for finding the latest answer for a specific question in a session
CREATE INDEX idx_user_answers_latest ON public.user_answers (session_id, question_id, attempt_sequence DESC, updated_at DESC, event_id DESC);

-- 5. SYNC EVENTS / OUTBOX ACK
-- Used for the server to track processed sync events and prevent duplicates/replays
CREATE TABLE public.sync_events (
    idempotency_key TEXT PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    event_type TEXT NOT NULL,
    payload JSONB,
    processed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    status TEXT NOT NULL -- 'ACKED', 'RESOLVED_DROPPED', 'NOOP_DUPLICATE'
);


-- ==========================================
-- ROW LEVEL SECURITY (RLS)
-- ==========================================

-- Enable RLS on all tables
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.bookmarks ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.saved_quizzes ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.quiz_sessions ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.user_answers ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.sync_events ENABLE ROW LEVEL SECURITY;

-- Profiles: Users can read and update their own profile
CREATE POLICY "Users can view own profile" ON public.profiles FOR SELECT USING (auth.uid() = id);
CREATE POLICY "Users can update own profile" ON public.profiles FOR UPDATE USING (auth.uid() = id);

-- Bookmarks
CREATE POLICY "Users can manage own bookmarks" ON public.bookmarks FOR ALL USING (auth.uid() = user_id);

-- Saved Quizzes
CREATE POLICY "Users can manage own saved quizzes" ON public.saved_quizzes FOR ALL USING (auth.uid() = user_id);

-- Quiz Sessions
CREATE POLICY "Users can manage own sessions" ON public.quiz_sessions FOR ALL USING (auth.uid() = user_id);

-- User Answers
CREATE POLICY "Users can manage own answers" ON public.user_answers FOR ALL USING (auth.uid() = user_id);

-- Sync Events
CREATE POLICY "Users can insert and read own sync events" ON public.sync_events FOR ALL USING (auth.uid() = user_id);
