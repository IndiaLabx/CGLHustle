CREATE OR REPLACE FUNCTION public.mark_session_synced_final(p_session_id uuid)
RETURNS jsonb
LANGUAGE plpgsql
SECURITY INVOKER
SET search_path = public
AS $$
DECLARE
    v_status public.session_status;
BEGIN
    SELECT status INTO v_status FROM public.quiz_sessions WHERE session_id = p_session_id;

    IF v_status = 'SYNCED_FINAL' THEN
        RETURN jsonb_build_object('status', 'noop_duplicate', 'message', 'Already SYNCED_FINAL');
    ELSIF v_status = 'SUBMITTED_LOCAL' THEN
        UPDATE public.quiz_sessions SET status = 'SYNCED_FINAL', updated_at = NOW() WHERE session_id = p_session_id;
        RETURN jsonb_build_object('status', 'applied', 'message', 'Transitioned to SYNCED_FINAL');
    ELSE
        RAISE EXCEPTION 'Cannot transition to SYNCED_FINAL from %', v_status;
    END IF;
END;
$$;
GRANT EXECUTE ON FUNCTION public.mark_session_synced_final TO authenticated;


CREATE OR REPLACE FUNCTION public.upsert_user_answer_safe(payload jsonb, clock_offset_ms integer)
RETURNS jsonb
LANGUAGE plpgsql
SECURITY INVOKER
SET search_path = public
AS $$
DECLARE
    tolerance_ms CONSTANT integer := 3000;
    v_user_id uuid := (payload->>'user_id')::uuid;
    v_session_id uuid := (payload->>'session_id')::uuid;
    v_question_id uuid := (payload->>'question_id')::uuid;
    v_event_id text := upper(payload->>'event_id');
    v_idempotency_key text := payload->>'idempotency_key';
    v_attempt_sequence int := (payload->>'attempt_sequence')::int;
    v_client_generated_at_ms bigint := (payload->>'client_generated_at_ms')::bigint;

    v_adjusted_client_time bigint := v_client_generated_at_ms + clock_offset_ms;
    v_server_received_at_ms bigint := extract(epoch from now()) * 1000;

    v_existing_ack record;
    v_existing_answer record;
    v_response jsonb;
    v_should_apply boolean := false;
    v_conflict_reason text := '';
BEGIN
    -- 1. Strict ULID Validation (Crockford Base32)
    IF NOT (v_event_id ~ '^[0-9A-HJKMNP-TV-Z]{26}$') THEN
        RAISE EXCEPTION 'Invalid ULID format for event_id';
    END IF;

    -- 2. Idempotency Ledger Check
    SELECT * INTO v_existing_ack FROM public.sync_events_ack
    WHERE user_id = v_user_id AND idempotency_key = v_idempotency_key;
    IF FOUND THEN
        RETURN v_existing_ack.response_jsonb;
    END IF;

    -- 3. Concurrency Lock
    PERFORM pg_advisory_xact_lock(hashtext(v_user_id::text || v_session_id::text || v_question_id::text));

    -- 4. Fetch the absolute latest answer for conflict resolution
    SELECT * INTO v_existing_answer FROM public.user_answers
    WHERE session_id = v_session_id AND question_id = v_question_id
    ORDER BY attempt_sequence DESC, updated_at DESC, event_id DESC
    LIMIT 1;

    -- 5. Deterministic Conflict Engine
    IF v_existing_answer IS NULL THEN
        v_should_apply := true;
    ELSIF v_attempt_sequence > v_existing_answer.attempt_sequence THEN
        v_should_apply := true;
    ELSIF v_attempt_sequence = v_existing_answer.attempt_sequence THEN
        -- Evaluate Timestamp fallback if within 3s boundary tolerance
        IF abs(v_adjusted_client_time - v_server_received_at_ms) <= tolerance_ms THEN
            -- We assume previous answer time was reliable enough, so we rely on Lexicographical ULID
            IF v_event_id > v_existing_answer.event_id THEN
                v_should_apply := true;
            ELSE
                v_conflict_reason := 'Tie-break: Lower lexicographical ULID';
            END IF;
        ELSE
            -- Boundary violation: Default to Lexicographical ULID anyway
            IF v_event_id > v_existing_answer.event_id THEN
                v_should_apply := true;
            ELSE
                v_conflict_reason := 'Tie-break: Lower lexicographical ULID (Time drifted)';
            END IF;
        END IF;
    ELSE
        v_conflict_reason := 'Lower attempt_sequence';
    END IF;

    IF v_should_apply THEN
        INSERT INTO public.user_answers (
            event_id, session_id, user_id, question_id, attempt_sequence,
            mutation_type, selected_option, client_generated_at
        ) VALUES (
            v_event_id, v_session_id, v_user_id, v_question_id, v_attempt_sequence,
            (payload->>'mutation_type')::answer_mutation_type,
            payload->>'selected_option',
            to_timestamp(v_client_generated_at_ms / 1000.0)
        );

        v_response := jsonb_build_object('status', 'applied', 'event_id', v_event_id);
    ELSE
        INSERT INTO public.sync_conflicts_log (user_id, table_name, event_id, payload, conflict_reason)
        VALUES (v_user_id, 'user_answers', v_event_id, payload, v_conflict_reason);

        v_response := jsonb_build_object('status', 'resolved_dropped', 'event_id', v_event_id, 'message', v_conflict_reason);
    END IF;

    -- 6. Write to Idempotency Ledger
    INSERT INTO public.sync_events_ack (user_id, idempotency_key, status, response_jsonb)
    VALUES (v_user_id, v_idempotency_key, v_response->>'status', v_response);

    RETURN v_response;
END;
$$;
GRANT EXECUTE ON FUNCTION public.upsert_user_answer_safe TO authenticated;

-- Placeholder for upsert_quiz_session_safe to maintain completeness
CREATE OR REPLACE FUNCTION public.upsert_quiz_session_safe(payload jsonb)
RETURNS jsonb
LANGUAGE plpgsql
SECURITY INVOKER
SET search_path = public
AS $$
DECLARE
    v_user_id uuid := (payload->>'user_id')::uuid;
    v_session_id uuid := (payload->>'session_id')::uuid;
    v_idempotency_key text := payload->>'idempotency_key';
    v_response jsonb;
    v_existing_ack record;
BEGIN
    SELECT * INTO v_existing_ack FROM public.sync_events_ack WHERE user_id = v_user_id AND idempotency_key = v_idempotency_key;
    IF FOUND THEN RETURN v_existing_ack.response_jsonb; END IF;

    PERFORM pg_advisory_xact_lock(hashtext(v_user_id::text || v_session_id::text));

    -- Insert / Update logic for quiz_session goes here
    v_response := jsonb_build_object('status', 'applied');

    INSERT INTO public.sync_events_ack (user_id, idempotency_key, status, response_jsonb)
    VALUES (v_user_id, v_idempotency_key, 'applied', v_response);

    RETURN v_response;
END;
$$;
GRANT EXECUTE ON FUNCTION public.upsert_quiz_session_safe TO authenticated;
