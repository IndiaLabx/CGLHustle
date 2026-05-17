# Error Recovery Contract

This contract formalizes the application's network and failure UX behavior to ensure robustness in offline-first and degraded-network scenarios.

## 1. Timeout Classes
Network requests are governed by specific timeout thresholds to prevent perpetual hanging and to allow the application to fallback gracefully:
- **Short Timeout (5s):** Fast-fail checks, such as querying backend health or verifying auth token validity.
- **Standard Timeout (15s):** Standard API read requests, lightweight sync payloads, or fetching specific quiz metadata.
- **Long Timeout (30s):** Heavy payloads, bulk snapshot fetching, or complex sync RPC transactions.

## 2. Retry and Backoff Policy for Sync Engine
The background sync engine utilizes WorkManager with exponential backoff:
- **Initial Retry:** 30 seconds after the first failure.
- **Backoff Multiplier:** 2x.
- **Maximum Retry Window:** Up to 24 hours. After 24h, the status remains retryable and foreground-triggered reconciliation should continue (it is not dead forever).
- **Failures Handled:** Network timeouts, 5xx server errors, DNS resolution failures. (Note: 4xx client errors like 400 or 403 do not trigger standard retries, as they signify structural issues).

## 3. Auth-Expiry Queue Behavior
When a user's session expires (e.g., Supabase JWT expiry):
- **Network Halting:** The sync engine halts all outgoing mutations and enters an `AUTH_REQUIRED` paused state. Any inflight requests returning a 401 Unauthorized immediately trigger this state.
- **Local Writes Allowed:** The user's offline-first experience remains uninterrupted. They can continue a quiz session, submit answers, and progress. These events are saved to the local Room database and queued in the sync ledger.
- **Recovery:** Upon successful re-authentication, the sync engine resumes the queue and processes the ledger sequentially.

## 4. Dual-Backend Outage UX
CGL Hustle integrates with an upstream content provider (GK LLM) and maintains its own backend (Supabase).
- **GK LLM Outage:**
    - The client cannot fetch new quiz content or metadata.
    - The UX displays a banner: "Unable to load new content right now."
    - Users **can** interact with any content already snapshotted and cached in the local Room DB (e.g., viewing past results or playing fully cached offline quizzes). Syncing progress to CGL Hustle backend continues normally.
- **CGL Hustle Outage:**
    - The app operates entirely in offline mode.
    - A banner displays: "Working offline. Progress will be saved automatically when reconnected."
    - Users can start cached quizzes, answer questions, and view local stats. All state changes are buffered in the Room ledger and will sync upon service restoration.
