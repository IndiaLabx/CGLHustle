ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.bookmarks ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.saved_quizzes ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.quiz_sessions ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.question_snapshots ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.user_answers ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.sync_events_ack ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.sync_conflicts_log ENABLE ROW LEVEL SECURITY;

-- Strict WITH CHECK enforcement on all mutations
CREATE POLICY "profiles_select" ON public.profiles FOR SELECT USING (auth.uid() = id);
CREATE POLICY "profiles_update" ON public.profiles FOR UPDATE USING (auth.uid() = id) WITH CHECK (auth.uid() = id);

CREATE POLICY "bookmarks_select" ON public.bookmarks FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "bookmarks_insert" ON public.bookmarks FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "bookmarks_update" ON public.bookmarks FOR UPDATE USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);
CREATE POLICY "bookmarks_delete" ON public.bookmarks FOR DELETE USING (auth.uid() = user_id);

CREATE POLICY "saved_quizzes_select" ON public.saved_quizzes FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "saved_quizzes_insert" ON public.saved_quizzes FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "saved_quizzes_update" ON public.saved_quizzes FOR UPDATE USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);
CREATE POLICY "saved_quizzes_delete" ON public.saved_quizzes FOR DELETE USING (auth.uid() = user_id);

CREATE POLICY "quiz_sessions_select" ON public.quiz_sessions FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "quiz_sessions_insert" ON public.quiz_sessions FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "quiz_sessions_update" ON public.quiz_sessions FOR UPDATE USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

CREATE POLICY "question_snapshots_select" ON public.question_snapshots FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "question_snapshots_insert" ON public.question_snapshots FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY "user_answers_select" ON public.user_answers FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "user_answers_insert" ON public.user_answers FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "user_answers_update" ON public.user_answers FOR UPDATE USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

CREATE POLICY "sync_events_ack_select" ON public.sync_events_ack FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "sync_events_ack_insert" ON public.sync_events_ack FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY "sync_conflicts_log_select" ON public.sync_conflicts_log FOR SELECT USING (auth.uid() = user_id);
