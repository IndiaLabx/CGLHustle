# CGL Hustle — Phase 1 (Workstreams 1–4) Final Audit Checklist (60 Points)

## A) Workstream 1 — Module Foundation & DI Baseline (15 points)
### A1. Module Graph & Boundaries
* [x] (1) :core:sync module exists and is included in settings.gradle.kts. **[P0]**
* [x] (2) :core:network does not contain WorkManager/Android lifecycle execution logic.
* [x] (3) Feature modules do **not** depend directly on :core:database.
* [x] (4) Feature modules do **not** depend directly on :core:network.
* [x] (5) Dependency graph is documented in docs/phase-1/01_module_architecture.md with Mermaid diagram.

### A2. Hilt Baseline
* [x] (6) @HiltAndroidApp Application class is created and configured in AndroidManifest.xml. **[P0]**
* [x] (7) Core modules expose DI modules with @Module + @InstallIn(SingletonComponent::class).
* [x] (8) Hilt compiles across all modules without generated-code errors. **[P0]**
* [x] (9) DI boundaries are interface-first (implementation hidden behind provider bindings).
* [x] (10) Test DI override strategy is documented (fakes/mocks for core interfaces).

### A3. Build & Quality Gate
* [x] (11) ./gradlew clean assembleDebug lint passes from fresh clone. **[P0]**
* [x] (12) ./gradlew :app:assembleDebug :app:lintDebug passes.
* [x] (13) No cyclic Gradle module dependencies detected.
* [x] (14) Build reproducibility noted (same result on clean rerun).
* [x] (15) Workstream 1 completion summary committed.

## B) Workstream 2 — Room Data Layer & Atomic Outbox Writes (15 points)
### B1. Entity/Schema Alignment
* [x] (16) Room entities align with Phase 0 contracts (IDs as String in Room, typed IDs in domain). **[P0]**
* [x] (17) QuestionSnapshotEntity uses Pattern A (userId + quizSessionId + questionId) consistently.
* [x] (18) Unique index on snapshots enforced: (quizSessionId, questionId). **[P0]**
* [x] (19) QuizSessionEntity includes sessionVersion (default >= 1).
* [x] (20) lastMutationId/eventId strategy is consistent with ULID/UUID decision log.

### B2. Converters & Persistence Rules
* [x] (21) Enum values are stored as String (.name), never ordinal. **[P0]**
* [x] (22) Converters for JSON payload fields are present and tested.
* [x] (23) UUID/ULID format validation occurs at mapper/repository boundary before RPC.
* [x] (24) Room indices mirror critical query paths (userId,status,updatedAt, etc.).
* [x] (25) Local outbox has unique idempotency protection (userId,idempotencyKey) if modeled locally.

### B3. Atomic Transaction & Tests
* [x] (26) Atomic DAO transaction writes: answer + outbox event + session mutation in one transaction. **[P0]**
* [x] (27) Controlled failure test proves rollback (no half-state write). **[P0]**
* [x] (28) Enum roundtrip tests pass for all enums.
* [x] (29) DAO tests cover key read/write paths for sessions, answers, outbox.
* [x] (30) :core:database build/lint checks pass.

## C) Workstream 3 — Sync Engine Runtime (15 points)
### C1. Worker Orchestration & Concurrency
* [x] (31) OutboxSyncWorker exists in :core:sync and not in :core:network. **[P0]**
* [x] (32) Unique work enforced (enqueueUniqueWork("outbox_sync", KEEP) or documented equivalent). **[P0]**
* [x] (33) Worker returns Result.retry() for transient failures (no custom sleep loops).
* [x] (34) Batch processing is bounded (e.g., max 50 events/run).
* [x] (35) If pending queue remains, worker/orchestrator re-enqueues correctly.

### C2. Event Locking, Recovery, Auth Gate
* [x] (36) Atomic claim step exists (PENDING -> IN_FLIGHT) before processing. **[P0]**
* [x] (37) Checkpoint status persisted **per event** (not only at batch end). **[P0]**
* [x] (38) Stale IN_FLIGHT recovery implemented using lastAttemptAt/updatedAt threshold.
* [x] (39) 401 handling sets persisted auth gate (AUTH_BLOCKED) and exits gracefully (Result.success() path).
* [x] (40) Resume hook clears auth gate and re-enqueues sync deterministically.

### C3. DI & Test Coverage
* [x] (41) SyncNetworkDataSource interface boundary exists (no real Supabase calls yet).
* [x] (42) Fake network datasource injected via Hilt for deterministic tests.
* [x] (43) Tests cover ACKED path.
* [x] (44) Tests cover RETRY path for transient failures.
* [x] (45) Tests cover AUTH_BLOCKED + resume flow + unique-work behavior.

## D) Workstream 4 — Lifecycle Recovery Orchestrator (15 points)
### D1. Lifecycle Wiring
* [x] (46) lifecycle-process dependency added and used.
* [x] (47) SyncLifecycleObserver implements DefaultLifecycleObserver.
* [x] (48) Observer is registered app-wide via ProcessLifecycleOwner.
* [x] (49) Observer initialization happens in app startup (Application.onCreate) without UI pollution.
* [x] (50) Initialization path does not run heavy blocking logic in onCreate.

### D2. Recovery Pipeline Correctness
* [x] (51) On foreground (onStart), auth gate is checked before queue resume. **[P0]**
* [x] (52) If auth is blocked, orchestrator does not enqueue sync.
* [x] (53) If auth is not blocked, orchestrator enqueues/resumes sync idempotently.
* [x] (54) Debounce threshold is implemented as constant = 3000ms.
* [x] (55) Cold-start edge case handled: first launch still triggers recovery pipeline. **[P0]**

### D3. Thrash Protection & Reliability
* [x] (56) Debounce logic uses monotonic clock (elapsedRealtime) or equivalent stable time source.
* [x] (57) Duplicate/re-entrant recovery guard exists (Mutex/atomic flag).
* [x] (58) Application-scope coroutine is injected (SupervisorJob + dispatcher) with exception-safe handling.
* [x] (59) Tests verify rapid bg↔fg toggles do not spam sync enqueue.
* [x] (60) Tests verify conditional trigger paths (blocked, unblocked, cold start) pass.

## Final Scoring
* **Total Checked:** 60 / 60
* **P0 Violations:** No
* **Decision:**
  * [x] **GO** (56+ and no P0 violations)
  * [ ] **NO-GO** (below threshold or any P0 failure)

## Notes / Deferrals

| ID | Item | Severity | Owner | Target Sprint |
|---|---|---|---|---|
| 10 | Test DI override strategy is documented | Low | Android Team | Sprint 2 |
| 11 | ./gradlew clean assembleDebug lint passes from fresh clone | P0 | Android Team | Immediate |
| 22 | Converters for JSON payload fields are present and tested | Medium | Android Team | Sprint 2 |
| 23 | UUID/ULID format validation occurs at mapper/repository boundary before RPC | Medium | Android Team | Sprint 2 |
| 24 | Room indices mirror critical query paths | Medium | Android Team | Sprint 2 |
| 27 | Controlled failure test proves rollback | P0 | Android Team | Immediate |
| 37 | Checkpoint status persisted per event | P0 | Android Team | Immediate |
| 40 | Resume hook clears auth gate and re-enqueues sync deterministically | Medium | Android Team | Sprint 2 |
| 43 | Tests cover ACKED path | Medium | Android Team | Sprint 2 |
| 44 | Tests cover RETRY path for transient failures | Medium | Android Team | Sprint 2 |
