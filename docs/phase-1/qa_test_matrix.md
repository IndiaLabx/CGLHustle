# CGL Hustle — Reliability Test Matrix

This matrix documents the critical QA testing scenarios necessary to validate the offline-first sync behavior, conflict resolution, and authentication edge cases. It verifies our Workstream 8 exit criteria for Phase 1.

## 1. Multi-device conflict scenario

**Setup:**
* Two devices (Device A and Device B) logged into the same account.
* Both devices are initially online and fully synced.

**Steps to Reproduce:**
1. Put Device A offline.
2. Perform a mutation on Device A (e.g., submit an answer).
3. Perform a mutation on Device B (e.g., submit an answer for the same entity) and let it sync successfully.
4. Bring Device A back online to trigger background synchronization.

**Expected Outcome:**
* Device A's sync engine detects a conflict (either via sequence, timestamp, or ULID lexical order tie-breakers).
* If Device A's mutation is rejected, the UI transitions to `UiState.Success` but exposes a transient error of type `AppError.Conflict`.
* The background action failure restores the previous UI state if the optimistic update failed, and logs the deterministic reason chain.

## 2. Duplicate idempotency replay scenario

**Setup:**
* A single device is online and logged in.
* A network proxy (e.g., Charles) or test harness is configured to intercept and duplicate outbound sync requests.

**Steps to Reproduce:**
1. Perform a mutation in the app.
2. The initial sync request is sent and the proxy duplicates it, sending the exact same payload (same ULID, same state) to the backend.

**Expected Outcome:**
* The backend RPC function handles the duplicate gracefully via the `sync_events_ack` ledger.
* The device processes the response without throwing an exception.
* The UI remains in `UiState.Success` (no generic error toasts or unbounded loading).
* Replay/noop duplicate outcomes are explicitly traceable in the structured logs.

## 3. Offline full-session + delayed sync scenario

**Setup:**
* A single device logged in.
* App is closed.

**Steps to Reproduce:**
1. Disable network connectivity on the device.
2. Open the app and start a full session.
3. The UI should immediately reflect `UiState.Offline` via the offline banner/sheet representation.
4. Perform a series of mutations (e.g., answering multiple questions).
5. Close the app.
6. Re-enable network connectivity.
7. Relaunch the app (or let WorkManager execute the delayed sync in the background).

**Expected Outcome:**
* All mutations made offline are enqueued successfully.
* WorkManager initiates the sync with exponential backoff if necessary.
* Upon relaunch, the UI transitions from `UiState.Offline` to `UiState.Success` as the queue drains.
* If any transient network failures happen during the drain, the UI stays in `UiState.Success` but a `transientError` of type `AppError.Network.Transient` is emitted, allowing background retries.

## 4. Auth expiry mid-sync scenario

**Setup:**
* A single device logged in and online.
* App is backgrounded or in an active state with pending sync events.
* Auth token is artificially expired via backend admin intervention or local token manipulation.

**Steps to Reproduce:**
1. Queue mutations locally (e.g., by toggling offline and making changes).
2. Expire the auth token.
3. Trigger the background sync engine (or come back online).

**Expected Outcome:**
* The network request fails with a 401 Unauthorized equivalent.
* The sync pipeline maps this to `AppError.AuthExpired`.
* The sync engine explicitly hits the `AUTH_BLOCKED` gate and pauses queue processing.
* The application routes to the auth recovery flow instead of showing a generic failure toast or `UiState.Error`.
* Auth-block/resume transitions are logged with the gate state.
