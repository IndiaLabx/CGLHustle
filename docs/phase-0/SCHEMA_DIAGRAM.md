# CGL Hustle - Schema Diagram

This Entity Relationship Diagram captures the deployed state of our core data models as defined in Phase 0 - Step 2.

```mermaid
erDiagram
    PROFILES ||--o{ BOOKMARKS : "has"
    PROFILES ||--o{ SAVED_QUIZZES : "has"
    PROFILES ||--o{ QUIZ_SESSIONS : "starts"
    PROFILES ||--o{ QUESTION_SNAPSHOTS : "owns"
    PROFILES ||--o{ USER_ANSWERS : "submits"
    PROFILES ||--o{ SYNC_EVENTS_ACK : "owns"
    PROFILES ||--o{ SYNC_CONFLICTS_LOG : "has"

    QUIZ_SESSIONS ||--o{ QUESTION_SNAPSHOTS : "contains"
    QUIZ_SESSIONS ||--o{ USER_ANSWERS : "receives"

    PROFILES {
        UUID id PK
        TEXT full_name
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
    }

    BOOKMARKS {
        UUID id PK
        UUID user_id FK
        UUID question_id "External reference contract (validated via ingestion/snapshot, not a DB FK)"
        TEXT notes
        TIMESTAMPTZ created_at
    }

    SAVED_QUIZZES {
        UUID id PK
        UUID user_id FK
        TEXT name
        JSONB filters
        TIMESTAMPTZ created_at
    }

    QUIZ_SESSIONS {
        UUID session_id PK
        UUID user_id FK
        UUID quiz_metadata_id
        session_status status
        INT session_version
        TEXT last_mutation_id "ULID"
        TIMESTAMPTZ start_time
        TIMESTAMPTZ last_paused_time
        TIMESTAMPTZ end_time
        BIGINT total_paused_duration_ms
        BIGINT active_duration_ms
        TIMESTAMPTZ client_generated_at
        TIMESTAMPTZ server_received_at
        TIMESTAMPTZ updated_at
    }
    note for QUIZ_SESSIONS "UNIQUE(user_id, last_mutation_id)\nCHECK(active_duration_ms >= 0)\nCHECK(total_paused_duration_ms >= 0)"

    QUESTION_SNAPSHOTS {
        TEXT snapshot_id PK "ULID"
        UUID user_id FK
        UUID quiz_session_id FK
        UUID question_id "External reference contract (validated via ingestion/snapshot, not a DB FK)"
        INT content_version
        TEXT snapshot_hash
        TEXT source_project
        BIGINT source_fetched_at
        BOOLEAN is_deleted_upstream
        TEXT question_text
        JSONB options
        TEXT correct_answer
        TEXT explanation
        TIMESTAMPTZ created_at
    }
    note for QUESTION_SNAPSHOTS "UNIQUE(quiz_session_id, question_id)"

    USER_ANSWERS {
        TEXT event_id PK "ULID"
        UUID session_id FK
        UUID user_id FK
        UUID question_id "External reference contract (validated via ingestion/snapshot, not a DB FK)"
        answer_mutation_type mutation_type
        TEXT selected_option
        INT attempt_sequence
        TIMESTAMPTZ client_generated_at
        TIMESTAMPTZ server_received_at
        TIMESTAMPTZ updated_at
    }
    note for USER_ANSWERS "UNIQUE(user_id, event_id)"

    SYNC_EVENTS_ACK {
        UUID id PK
        UUID user_id FK
        TEXT idempotency_key
        TEXT status
        TEXT request_hash
        JSONB response_jsonb
        TIMESTAMPTZ processed_at
    }
    note for SYNC_EVENTS_ACK "UNIQUE(user_id, idempotency_key)"

    SYNC_CONFLICTS_LOG {
        UUID id PK
        UUID user_id FK
        TEXT table_name
        TEXT event_id "ULID"
        JSONB payload
        TEXT conflict_reason
        TIMESTAMPTZ created_at
    }
```
