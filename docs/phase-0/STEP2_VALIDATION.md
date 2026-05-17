# Step 2 Validation Report

This report documents the validation of the Supabase backend configuration to ensure production-readiness for Phase 0 Step 2.

## 1. Migration Execution Details
All 5 SQL migration files in `supabase/migrations/` executed cleanly and idempotently.
- `20260517_01_create_enums.sql` created the required enums `session_status` and `answer_mutation_type`.
- `20260517_02_create_core_tables.sql` built the base tables mapping directly to the client's local Room entities. The `question_id` acts as an external reference contract (validated via ingestion/snapshot, not a DB FK).
- `20260517_03_indexes_constraints.sql` successfully applied the composite UNIQUE constraints and duration checks.
- `20260517_04_rls_policies.sql` deployed strict RLS tenant isolation using `auth.uid()`.
- `20260517_05_sync_rpcs.sql` established the stored procedures for safe data synchronization.

## 2. Row Level Security (RLS) Enforcement
The core database utilizes Postgres RLS to enforce tenant isolation.
Policies explicitly define conditions for `SELECT`, `INSERT`, `UPDATE`, and `DELETE`.
Specifically, `INSERT` and `UPDATE` statements are guarded by `WITH CHECK (auth.uid() = user_id)`.
This guarantees that a user cannot insert or alter a row by manipulating the `user_id` column to spoof another user; Postgres strictly denies operations where the data's target `user_id` does not match the authenticated user's JWT ID. There is no fallback for anonymous or cross-tenant data mutation.

## 3. Idempotency Proof
The `sync_events_ack` table operates as a ledger for client requests. It guarantees idempotency through a composite unique constraint: `UNIQUE (user_id, idempotency_key)`.
When a sync RPC, such as `upsert_user_answer_safe`, is triggered:
1. It queries `sync_events_ack` using the provided `idempotency_key` and `user_id`.
2. If a matching record is found, the RPC halts further processing and immediately returns the cached `response_jsonb`.
3. This prevents a retry of the same network request from duplicating side effects or corrupting data.

## 4. Conflict Resolution Dry-Run Theory (`upsert_user_answer_safe`)
The deterministic conflict engine is implemented within `upsert_user_answer_safe` and behaves according to the following logic:

- **Scenario 1: Higher Sequence:** If the incoming payload has a higher `attempt_sequence` than the stored answer, it is applied (client progresses normally).
- **Scenario 2: Lower Sequence:** If the `attempt_sequence` is lower, the server rejects it as stale and logs the attempt to `sync_conflicts_log` with the reason "Lower attempt_sequence".
- **Scenario 3: Same Sequence + Newer Time (Within Tolerance):** If sequences match, the server checks the client time (adjusted for drift) against the server time. If the difference is $\le$ 3000ms, it falls back to a lexicographical ULID tie-breaker.
- **Scenario 4: Tie-Break ULID:** If sequences match and times drift beyond tolerance (or as the final fallback mechanism), the incoming payload is only accepted if its `event_id` is lexicographically greater than the stored `event_id`. Otherwise, it logs a "Tie-break: Lower lexicographical ULID" conflict.

## 5. Constraint Checks
Negative durations for active or paused quiz times are impossible.
The `quiz_sessions` table includes two `CHECK` constraints:
- `chk_quiz_sessions_active_duration CHECK (active_duration_ms >= 0)`
- `chk_quiz_sessions_paused_duration CHECK (total_paused_duration_ms >= 0)`
Attempting to insert or modify a session with negative values results in a Postgres constraint violation error returned; mapped to structured application error code. This protects the integrity of timer calculations.
