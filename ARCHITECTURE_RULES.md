# CGL Hustle — Official Architecture Rules

This document outlines the strict engineering laws for the CGL Hustle Android application. These rules protect the application from architectural drift, specifically preventing the reintroduction of offline-first, sync-heavy, or non-native web patterns.

## 1. Server-First Rules
*   **The Backend is the Single Source of Truth:** The Supabase backend is the authoritative brain. The Android app is the intelligent, real-time interface layer.
*   **No "Local-First" Sync Engines:** The application is a connected cloud-native Android app, NOT a distributed offline replication system.
*   **Optimistic UI:** User interactions should update in-memory state and the UI instantly, followed by immediate direct network calls to the server.

## 2. Runtime State Ownership & Compose Principles
*   **ViewModel Ownership:** ViewModels exclusively own and orchestrate the active runtime state.
*   **In-Memory StateFlow:** Compose UI MUST observe `StateFlow` exposed by ViewModels.
*   **No Reactive Room Pipelines:** Compose UI MUST NOT be driven by massive Room combine chains or direct `Flow<Entity>` observations from the database during active flows (e.g., Quiz sessions).

## 3. Room Database Limitations
*   **Lightweight Fallback Only:** Room is currently used as lightweight runtime resilience infrastructure, not as the authoritative application state layer.
*   **Valid Uses:** Temporary session recovery, crash continuity, tiny runtime buffering, and lightweight bookmark caching.
*   **Invalid Uses:** Primary reactive state graphs, giant sync orchestration, authoritative quiz persistence, or offline-first runtime architecture.

## 4. WorkManager & Sync Limitations
*   **Resilience, Not Backbone:** WorkManager is a fallback resilience mechanism. It is NOT the backbone of the application runtime.
*   **Valid Uses:** Retry-safe uploads, interrupted sync recovery, and crash-safe persistence retries (e.g., `OutboxSyncWorker`).
*   **Invalid Uses:** Continuous synchronization orchestration, event-sourced runtime systems, or giant outbox architectures.

## 5. Navigation Rules
*   **Session-Driven Routing:** Navigation routing at the root level must be driven intelligently by the Supabase session state.
*   **Strict Replace Transitions:** Flows that finalize a state (e.g., finishing a quiz and moving to results) MUST use a [Replace] transition (clearing the active runtime path from the backstack) so users cannot press 'back' into stale states.
*   **Auth Backstack Cleared:** Successful authentication MUST clear the auth backstack completely.

## 6. Authentication & Identity Rules
*   **Centralized Identity:** No hardcoded User IDs or "mock users" are permitted in runtime systems.
*   **Supabase Authority:** Quiz systems and backend mutations must rely strictly on the authenticated Supabase session as the identity authority.
*   **Explicit Session Hydration Contract:**
    1.  Fetch from server FIRST.
    2.  Hydrate ViewModel memory state.
    3.  Fallback to lightweight Room cache ONLY if needed.
    4.  Begin runtime interaction.

## 7. Standardized ViewModel Responsibility Pattern
The correct and official pattern for all features is:
```text
Compose UI
← ViewModel StateFlow (In-memory Optimistic State)
← Repository
← Supabase Backend (Authoritative)
```

**Do NOT implement:**
```text
Compose UI
← Room reactive graph
← sync engine
← retry orchestration
```

*By adhering to these rules, the CGL Hustle codebase remains a premium, scalable, and responsive cloud-native Android ecosystem.*
