# State Machine Contract

This contract defines the strict lifecycle and legal transitions for `quiz_sessions`. State is primarily driven by the client and reconciled on the server.

## 1. Session Lifecycle States
The valid states for a quiz session (mapped to `session_status` enum) are:
- `NOT_STARTED`: The session has been initialized locally but the user has not triggered the start action.
- `IN_PROGRESS`: The user is actively engaging with the quiz. The active timer is running.
- `PAUSED`: The quiz is temporarily suspended. The active timer is halted, and the paused timer is running.
- `SUBMITTED_LOCAL`: The user has explicitly finished the quiz. Local grading can occur. No new answer mutations accepted from UI. Pending outbox drain is allowed. Only transitions to SYNCED_FINAL or TERMINATED_CONFLICT.
- `SYNCED_FINAL`: The server acknowledges receipt of the final submission. The session is immutable.
- `TERMINATED_CONFLICT`: The session was terminated due to irrecoverable sync state (e.g., upstream content deletion during play).
- `ABANDONED`: The user explicitly discarded the session without completing it.

## 2. Legal Transitions
A session may only traverse specific paths:
- `NOT_STARTED` -> `IN_PROGRESS`, `ABANDONED`
- `IN_PROGRESS` -> `PAUSED`, `SUBMITTED_LOCAL`, `ABANDONED`, `TERMINATED_CONFLICT`
- `PAUSED` -> `IN_PROGRESS`, `SUBMITTED_LOCAL`, `ABANDONED`
- `SUBMITTED_LOCAL` -> `SYNCED_FINAL`, `TERMINATED_CONFLICT`
- `SYNCED_FINAL` -> *Terminal State*
- `TERMINATED_CONFLICT` -> *Terminal State*
- `ABANDONED` -> *Terminal State*

## 3. Invalid Transition Behavior
The system employs strict validation to reject illegal state changes:
- **Server Enforcement:** The RPC `mark_session_synced_final` explicitly prevents transitioning a session out of `SYNCED_FINAL`. For example, if a rogue client sends an `IN_PROGRESS` payload for a session already marked `SYNCED_FINAL`, the server will reject the payload and log the conflict.
- **Client Enforcement:** The local Room repository will throw an `IllegalStateException` if a user attempts to pause a `NOT_STARTED` session or submit a `TERMINATED_CONFLICT` session.

## 4. Multi-Device Race Resolution
If a user is logged into multiple devices (e.g., Phone A and Phone B) and attempts concurrent actions on the same session ID:
- **State Dominance:** Progressing states (`SUBMITTED_LOCAL`, `SYNCED_FINAL`, `ABANDONED`) inherently trump active states (`IN_PROGRESS`, `PAUSED`).
- If Phone A submits a session while Phone B is `IN_PROGRESS` and offline, Phone A's state syncs to `SYNCED_FINAL`. When Phone B comes online and attempts to sync its `IN_PROGRESS` payload, the server rejects it. Phone B will fetch the updated session state, notice `SYNCED_FINAL`, and drop its local `IN_PROGRESS` state to match the server.
- The `session_version` integer acts as an optimistic lock for concurrent state changes.
