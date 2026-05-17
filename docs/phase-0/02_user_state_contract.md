# 02 User-State Contract: Lifecycle & Safety

## 1. Overview
This document outlines the volatile user progress, answers, and analytics states owned by the Android CGL Hustle backend. It defines strict state machine transitions, concurrent version control, and absolute metric integrity to ensure resilient offline and cross-device functionality.

## 2. Session Lifecycle States
The `quiz_sessions` table uses a strict `SessionStatus` enum. State transitions are strictly validated to prevent illegal operations.

### 2.1 State Transition Machine
| From-State | Allowed Event | To-State | Outbox Enqueue? |
| :--- | :--- | :--- | :--- |
| `[None]` | Start Session | `NOT_STARTED` | Yes (UPSERT_SESSION) |
| `NOT_STARTED` | Begin Answering | `IN_PROGRESS` | Yes (UPSERT_SESSION) |
| `NOT_STARTED` | Discard | `ABANDONED` | Yes (if previously synced) |
| `IN_PROGRESS` | Pause Session | `PAUSED` | Yes (UPSERT_SESSION) |
| `IN_PROGRESS` | Submit Final | `SUBMITTED_LOCAL` | Yes (MARK_COMPLETED) |
| `IN_PROGRESS` | Discard | `ABANDONED` | Yes |
| `PAUSED` | Resume Session | `IN_PROGRESS` | Yes (UPSERT_SESSION) |
| `PAUSED` | Discard | `ABANDONED` | Yes |
| `SUBMITTED_LOCAL`| Sync Engine ACK | `SYNCED_FINAL` | No (ACK handles it) |
| `SUBMITTED_LOCAL`| Server Rejection | `TERMINATED_CONFLICT`| No (Requires manual/code recovery) |

*Behavior for invalid events:* Any transition not explicitly defined above (e.g., `PAUSED` to `NOT_STARTED`) must be actively rejected (No-Op) by the state machine and logged as a state violation.

### 2.2 `SUBMITTED_LOCAL` Clarification
While the user interface (UI) strictly blocks new answer mutations in the `SUBMITTED_LOCAL` state, the **Sync Engine** is explicitly allowed to continue draining pending queued answer events from the outbox. If server-side correction events or the final acknowledgement arrives, the state transitions to `SYNCED_FINAL` or `TERMINATED_CONFLICT`.

## 3. Concurrency & Version Control
Because a user might switch between an Android phone and a tablet while entirely offline, we employ strict monotonic versioning to serialize distributed state.

### 3.1 Version Control Mechanics
- **`sessionVersion`**: An integer incremented monotonically upon every state change or configuration mutation of the session itself.
- **`lastMutationId`**: A **ULID** string referencing the absolute latest mutation event applied to this session.
Any incoming payload to the server must present an expected `sessionVersion` and `lastMutationId`. Out-of-order session updates are rejected or queued for rebase.
- **Payload Schema Version:** All payloads include `schemaVersion: Int` to handle backward-compatible parsing logic. Payload compression flags (`isCompressed: Boolean`) are supported for large session state blobs.

## 4. Timer & Metrics Integrity
Standard "start time vs end time" calculations fail spectacularly in offline-first systems or when device clocks drift. CGL Hustle employs incremental active time tracking.

### 4.1 Incremental Tracking
- **`activeDurationMs`**: Monotonically increments only when the user is in the `IN_PROGRESS` state.
- **`totalPausedDurationMs`**: Accumulates the time spent in the `PAUSED` state.
This completely eliminates device-switch timer anomalies because the elapsed time is accumulated purely locally based on verified operational deltas, rather than absolute system clock spans.

### 4.2 Server Cross-Check (Authoritative Truth)
The **Server is the ultimate authoritative source** for timer integrity. The server cross-checks the declared `activeDurationMs` against the server-observed timestamps and network handshakes.
- **Tolerance:** The server enforces a **±3 second** boundary tolerance.
- **Anomaly Handling:** If the client's reported duration violates this tolerance, the submission is **accepted** (preventing data loss or blocking user flow), but it is explicitly **flagged for audit**.
- **Penalty:** Leaderboard eligibility is suspended if a session is flagged.
- Do *not* trigger retry-resubmission for timer anomalies; they are definitively terminal state.

### 4.3 Requested Telemetry Events
The following 4 telemetry events are tracked alongside session transitions:
1. `session_started`: Fired on transition to `NOT_STARTED` / `IN_PROGRESS`.
2. `session_paused`: Fired when transitioning to `PAUSED`.
3. `session_resumed`: Fired when transitioning back to `IN_PROGRESS`.
4. `session_submitted`: Fired on transition to `SUBMITTED_LOCAL`.
