# 03 Sync Protocol Contract: Idempotency & Conflict Resolution

## 1. Overview
This document is the absolute blueprint for the CGL Hustle multi-device, offline-first sync engine. It defines outbox architectures, strict idempotency generation rules, and the mathematical fallback logic used by the server to safely merge conflicting concurrent device states.

## 2. Outbox Schema Architecture
The local client writes all operational mutations to a local outbox queue (`sync_events` table). All event identifiers (`eventId`) across the system must use **ULID** strings exclusively.

### 2.1 SyncEvent Schema
- `id`: Auto-generated primary key (Local SQLite sequence).
- `eventId`: **ULID** string representing the unique mutation event.
- `eventType`: The action type (e.g., `UPSERT_SESSION`, `UPSERT_ANSWER`).
- `payload`: The serialized JSON representation of the entity mutation (must include `schemaVersion: Int` and `isCompressed: Boolean`).
- `status`: Tracking enum.
- `createdAt`: Local Unix timestamp.
- `nextRetryAt`: Exponential backoff timestamp.
- `retryCount`: Integer tracking retry attempts.
- `lastErrorCode`: Captured HTTP or validation error string.

### 2.2 Outbox Status Machine
The outbox events flow through a strict state machine:
- **`PENDING`**: Ready to be sent to the network.
- **`PENDING_AUTH`**: (Auth-Specific Partitioning) Authentication token is invalid/expired. The network queue halts completely to prevent 401 spam. However, the local UI and local DB writes continue normally. The queue resumes only upon receiving an `AUTH_RESTORED` event.
- **`IN_FLIGHT`**: Currently being processed by the network layer.
- **`ACKED`**: Terminal. The server successfully processed the event.
- **`FAILED_RETRY`**: Transient failure (e.g., 500, Timeout). Exponential backoff logic determines `nextRetryAt`. Max retries policy: 5 attempts.
- **`RESOLVED_DROPPED`**: Terminal. The server rejected the event due to conflict resolution (e.g., a newer event superseded it), but the resolution is considered safely handled.
- **`FAILED_FATAL`**: Terminal. Unrecoverable error (e.g., 400 Bad Request payload validation failure). Triggers dead-letter cleanup policy (flagged for review, optionally purged after 30 days).

## 3. Deterministic Idempotency Key Specification
To absolutely prevent duplicate network replays from corrupting state, every outbox event contains a globally unique, deterministic string.

### 3.1 Key Generation Templates
- **Answer Mutations:**
  `userId:sessionId:questionId:attemptSequence:eventId` (where `eventId` is a ULID)
- **Session States:**
  `userId:sessionId:sessionVersion:eventId` (where `eventId` is a ULID)

The server enforces a unique constraint on `idempotency_key` within the `sync_events` acknowledgment table. Duplicate payloads result in an immediate `NOOP_DUPLICATE` response, allowing the client to safely ACK the event.

## 4. Multi-Device Conflict Resolution Engine
When concurrent offline modifications occur (e.g., answering the same question differently on two offline devices, then connecting to the internet), the server implements an absolute, deterministic tie-breaking sequence.

### 4.1 Resolution Sequence
1. **Higher `attemptSequence` Wins:**
   The absolute primary deterministic vector. If Device A submits `attemptSequence=2` and Device B submits `attemptSequence=3`, Device B wins regardless of timestamps.
2. **Newer Adjusted Timestamp Wins (Clock Drift Adjusted):**
   If devices assert an identical `attemptSequence` but have conflicting timestamp values, the server evaluates the **True Server Ingestion Order** using strict field math.
3. **Lexicographical ULID Comparison:**
   If the calculated time delta reveals an impossible boundary condition, the server falls back to standard string comparison of the `eventId` (ULID).
4. **Exact Match (`NOOP_DUPLICATE`):**
   If all identifiers and payloads match exactly, it is treated as a safe network replay.

### 4.2 Clock Skew Mathematical Protocol (Concrete Formula for Step 2)
To neutralize client-side clock tampering or extreme drift during Step 2, the server calculates the estimated true age of the mutation event.

**Variables:**
- `clientGeneratedAtMs`: Extracted from the client event payload.
- `serverReceivedAtMs`: Captured atomically by the server upon request receipt.
- `clockOffsetMs`: Established during the synchronization handshake (e.g., time synchronization request).

**Formula:**
`adjustedClientTime = clientGeneratedAtMs + clockOffsetMs`

**Validation Strategy:**
The server compares the `adjustedClientTime` against `serverReceivedAtMs`.
- **Boundary Constraint:** If the absolute difference `|adjustedClientTime - serverReceivedAtMs|` is greater than a **3-second boundary tolerance**, the calculated timestamp is considered unreliable (impossible future/past drift).
- If reliable, the newer `adjustedClientTime` wins.
- If the boundary is violated, the raw timestamp is discarded, and the conflict engine immediately proceeds to Step 3.

### 4.3 Tie-breaking Fallback (ULID String Comparison)
When Step 2 fails due to extreme clock skew (or exact timestamp collisions), the system leverages the structural properties of ULIDs.
- The `eventId` must be formatted as a standard ULID (Universally Unique Lexicographically Sortable Identifier).
- The server performs a standard lexicographical string comparison on the `eventId` strings.
- **The lexicographically larger ULID string always wins.** This guarantees a deterministic resolution across all nodes.
