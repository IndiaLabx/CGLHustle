# 01 Content Contract: Read Models & Snapshot Rules

## 1. Overview
This document defines how the CGL Hustle application handles read-only educational content ingested from the external **GK LLM** project. To guarantee offline stability, cross-device consistency, and immunity from upstream content mutations during active test sessions, all content must be strictly snapshotted and cryptographically verified.

## 2. Ingestion & ExamMetadata Structural Boundary
The upstream `public.questions` table in the GK LLM database is completely flat. Therefore, `ExamMetadata` is **not** a separate relational table but a logical grouping derived directly from the content columns.

### 2.1 ExamMetadata Logical Schema
On the Android client (Room), `ExamMetadata` is represented as a local tracking object or embedded entity that dictates the boundaries of a generated test session. It maps to the following explicit types to ensure perfect session recreation:
- `examName`: `String`
- `examYear`: `Int`
- `examDateShift`: `String`
- `subject`: `String`
- `topic`: `String`
- `fullAppliedFilterSet`: `String` (Stored as JSON representing the exact query parameters used)
- `randomizationSeed`: `Long` (For deterministic test reconstruction if ordered randomly)
- `selectionStrategyId`: `String` (e.g., `'strict_random'`, `'spaced_repetition'`)

**Initialization Flow:** When a test session is initialized, the app queries the GK LLM API using these filters. The matching rows are cloned into the local Room database as immutable `QuestionSnapshotEntity` records. The `quizSessionId` is appended to these records to anchor them explicitly to that specific user attempt.

### 2.2 QuestionSnapshot Schema Definition
The `QuestionSnapshot` guarantees that a user sees the exact same question text and options for a specific session attempt, even if the upstream database modifies the question later.

**Room Entity (`QuestionSnapshotEntity`) / Postgres Representation:**
- `snapshotId`: `String` (ULID, Primary Key for local storage to prevent cross-session collisions)
- `quizSessionId`: `UUID` (Logical anchor to the current test attempt)
- `questionId`: `UUID` (The upstream GK LLM ID)
- *Constraint:* `UNIQUE(quizSessionId, questionId)` ensures a question appears only once per session.
- `contentVersion`: `Int` (Tracks structural schema versions of the payload)
- `snapshotHash`: `String` (SHA-256 hex string, detailed below)
- `sourceProject`: `String` (e.g., `'gk_llm'`)
- `sourceFetchedAt`: `Long` (Unix timestamp in milliseconds)
- `languagePackVersion`: `String` (Identifies the localization version applied)
- `isDeletedUpstream`: `Boolean` (Fallback flag if content is retired but needed for review)
- `subject`: `String`
- `topic`: `String`
- `difficulty`: `String`
- `questionType`: `String`
- `questionText`: `String`
- `questionTextHi`: `String?`
- `options`: `String` (Stored as JSON array)
- `optionsHi`: `String?` (Stored as JSON array)
- `correctAnswer`: `String`
- `explanation`: `String?`
- `tags`: `String?` (Stored as JSON list)
- `schemaVersion`: `Int` (Payload schema version for cross-platform parsing alignment)

## 3. Data Integrity & Snapshot Validation
To detect cross-device payload corruption or data drift mid-session, a strict payload normalization and hashing routine must be enforced. Any device evaluating the exact same content payload must arrive at an identical hash value.

### 3.1 Deterministic `snapshotHash` Computation Protocol
1. **Target Fields Only:** The hash evaluates *only* structurally immutable content fields. Transient local metadata (`snapshotId`, `quizSessionId`, `sourceFetchedAt`, `isDeletedUpstream`, etc.) are explicitly excluded.
2. **Canonical JSON Generation:** Serialize exactly the following fields:
   `questionId`, `questionText` (mapped to `question`), `questionTextHi` (mapped to `question_hi`), `options`, `optionsHi` (mapped to `options_hi`), `correctAnswer` (mapped to `correct`), and `explanation`.
3. **Strict Normalization Rules:** To guarantee identical hashes across Kotlin/Swift/JS platforms:
   - **Null Handling:** Explicitly emit null values as JSON `null` (do not omit keys or emit empty strings `""`).
   - **Unicode Normalization:** Require Unicode Normalization Form C (NFC) for all strings.
   - **Numbers:** Integers must have no decimal point. Floats must not use scientific notation unless strictly exceeding language bounds (stick to 6 decimal precision formatting if applicable).
   - **Sorting:** Require recursive alphabetical key-sorting for all JSON objects (including nested structures like `explanation`).
   - **Booleans:** Enforce strictly lowercase booleans (`true`/`false`).
   - **Whitespace/Newlines:** Minify the string (remove all unescaped whitespace). Normalize newlines strictly to `\n` (stripping all `\r`).
   - **Arrays:** Array elements within `options`, `optionsHi`, and `tags` must perfectly preserve their exact structural database index ordering.
4. **Hashing Algorithm:** Apply standard **SHA-256** on the resulting UTF-8 string encoding. The final output is stored as a hex string representation in the `snapshotHash` field.

## 4. Bilingual & Lifecycle Handling
- **`languagePackVersion`**: Used to coordinate UI localization and content fallbacks if a specific translation is unavailable or structurally altered.
- **`sourceProject` & `sourceFetchedAt`**: Provides a deterministic audit trail linking the snapshot back to the exact ingestion pipeline and time.
- **`isDeletedUpstream`**: If a question is retired or flagged in the GK LLM source, active sessions relying on the snapshot are not disrupted. During subsequent syncs, the backend flips this flag to `true`, preventing the question from appearing in *new* exams while allowing the user to review their completed test seamlessly.
