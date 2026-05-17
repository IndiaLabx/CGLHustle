# Architectural Decision Log

This log freezes the core architectural decisions and governance policies established during Phase 0 for CGL Hustle.

## 1. UUID vs ULID Policy
- **Decision:** UUIDs are mandated for structural identity referencing external systems and users (e.g., `user_id`, `session_id`, `quiz_metadata_id`, `question_id`). ULIDs are mandated for client-generated events and local chronological sorting (e.g., `event_id`, `last_mutation_id`, `snapshot_id`).
- **Rationale:** UUIDs ensure compatibility with Supabase Auth and the upstream GK LLM flat schema. ULIDs provide lexicographical sortability which is crucial for deterministic offline-first conflict resolution.
- **Strict Validation Rule:** All ULIDs must be exactly 26 characters long, utilize the Crockford Base32 character set, and enforce uppercase normalization. The backend RPCs will reject any payload with an invalid ULID.

## 2. Idempotency Key Structure
- **Decision:** Idempotency keys must enforce strict tenant isolation via a composite unique constraint in the ledger: `UNIQUE (user_id, idempotency_key)`.
- **Rationale:** Prevents cross-tenant pollution. A malicious actor cannot replay an idempotency key from User A into User B's session to bypass validation or corrupt data. The ledger is bound to the `user_id` enforced by RLS.

## 3. Conflict Tie-Break Order
- **Decision:** The offline-first sync engine resolves answer conflicts deterministically using the following exact order of precedence:
  1. **attemptSequence:** Higher attempt sequence always wins.
  2. **adjusted client time (within tolerance):** If sequences tie, the newer timestamp wins, provided the client clock drift is within the $\pm$ 3000ms tolerance boundary.
  3. **ULID lexical:** If times are outside tolerance (or exactly tied), the lexicographically greater ULID wins.
  4. **noop duplicate:** If all attributes match, the payload is treated as a safe duplicate/noop.
- **Rationale:** Guarantees that eventually-consistent devices resolve to the exact same state, neutralizing network ordering unpredictability.

## 4. RPC Security Mode
- **Decision:** All synchronization Remote Procedure Calls (RPCs) are configured with `SECURITY INVOKER`.
- **Rationale:** This ensures the stored procedures execute with the permissions of the calling user (the authenticated JWT context), inheriting the strict Row Level Security (RLS) policies. Using `SECURITY DEFINER` would bypass RLS and create unacceptable data isolation risks.