# CGL Hustle — Architecture Audit Report

This document serves as the official architectural alignment audit for the CGL Hustle Android app. Its purpose is to verify current codebase alignment with the official "Server-First, Native Android" architecture guide, ensuring no offline-first, sync-heavy, or non-native web patterns pollute the system.

---

## 1. Executive Summary

Overall, the architectural health of the application is **Strong**.
The repository successfully avoids the "giant offline-first Room database" anti-pattern. State management relies heavily on Compose-friendly in-memory `StateFlow` structures, and persistence is appropriately treated as a fallback mechanism. The architecture demonstrates a healthy separation of concerns using Hilt, Jetpack Compose, and Kotlin Coroutines.

---

## 2. Current Architecture Snapshot

The application currently behaves as a cloud-native Android client:
- **UI State**: Compose components observe `StateFlow` exposed by ViewModels.
- **Optimistic UI**: ViewModels handle user actions (like selecting quiz answers) by updating in-memory state immediately.
- **Repository Execution**: Network requests are executed directly in Coroutine scopes.
- **Database Role**: Room is currently used as lightweight runtime resilience infrastructure, not as the authoritative application state layer.
- **Sync/WorkManager**: A dedicated `OutboxSyncWorker` exists strictly as a fallback resilience mechanism for when direct network calls fail.

---

## 3. Critical Violations

**None observed that fundamentally break the Server-First paradigm.**
The architecture successfully completely avoids the critical violation of Room driving the active runtime UI via continuous `Flow<Entity>` observation during active quiz sessions.

---

## 4. Moderate Concerns

- **Navigation Backstack on Quiz Finish**: In `NavGraph.kt`, moving from `activeSession` to `results` utilizes `popUpTo("quizConfig") { inclusive = false }`. The architecture guide dictates that finishing a quiz should **[Replace]** the flow to `QuizResultRoute`. A slightly tighter backstack pop rule (popping the active session explicitly to ensure users cannot hit "back" into an active quiz state) might be necessary for a true replace transition.
- **Hardcoded Identifiers**: Currently, there are hardcoded User IDs (e.g., `"mock_user_id"`) in view models like `ActiveSessionViewModel`. This needs to be correctly wired to the global Supabase Auth state.
- **Legacy Naming in Memory**: While the codebase uses `CGLHustle`, some admin domain logic (e.g., `admin@mindflow.com` in `DashboardViewModel`) is currently tolerated as intentional temporary infrastructure.

---

## 5. Positive Architecture Patterns

- **✅ ViewModel-owned in-memory StateFlow**: The `ActiveSessionViewModel` correctly orchestrates state purely in memory (`MutableStateFlow<UiState<ActiveSessionData>>`).
- **✅ Immediate server synchronization & Optimistic UI**: When an answer is selected, state updates immediately, and the `ActiveSessionRepositoryImpl` makes a direct network call (`syncNetworkDataSource.submitAnswer`) FIRST, before touching Room.
- **✅ Lightweight Room fallback usage**: Room is used simply to log the answer and, if the network call fails, queues a `SyncEventEntity` for the WorkManager.
- **✅ Clean repository boundaries**: Interfaces separate the UI intent from the implementation logic successfully.
- **✅ Navigation consistency**: The `MainActivity` correctly observes the `sessionStatus` from the AuthRepository and dynamically routes to `dashboard` or `auth`, successfully clearing the backstack.

---

## 6. Recommended Refactor Priority

1. **Authentication State Injection**: Replace hardcoded `mock_user_id` in `ActiveSessionViewModel` with a reactive user session flow provided by the `AuthRepository`.
2. **Refine Quiz Session Navigation**: Explicitly use `popUpTo(0)` or precise inclusive popping when moving from `ActiveSession` to `Results` to guarantee a clean **[Replace]** transition as mandated by the guide.
3. **Admin Identity Migration**: While currently ignored, plan to migrate `admin@mindflow.com` checks to a backend role-based or updated branding approach.

---

## 7. Runtime State Analysis

- **Ownership**: Runtime state is firmly owned by ViewModels (`QuizConfigViewModel`, `ActiveSessionViewModel`, `ResultsViewModel`).
- **Mechanism**: State is pushed to the UI via `StateFlow` and user interactions mutate this state optimistically before network confirmation.
- **Verdict**: Perfectly aligned with a "fast connected cloud-native Android app".

---

## 8. Room Database Dependency Analysis

- **Usage**: Room is strictly a fallback.
- **Observation**: While DAOs expose reactive methods (e.g., `observeAllAnswersForSession`), ViewModels are **not** observing these to build massive UI combination pipelines.
- **Verdict**: Correctly used as lightweight buffering and retry resilience.

---

## 9. WorkManager & Sync Analysis

- **Complexity**: `OutboxSyncWorker` is simple and predictable. It reads pending events, attempts to push them, updates checkpoints, and handles basic conflict states.
- **Orchestration**: It does not run continuously. It is triggered only when a direct mutation fails, ensuring it acts as a resilience layer, not the runtime backbone.
- **Verdict**: Aligned with the requirement to NOT use WorkManager for continuous orchestration.

---

## 10. Navigation & Session Architecture

- **Auth Flow**: Handled at the root level via `MainActivity`. Unauthenticated users are routed to `auth` and backstacks are cleared.
- **Dashboard Flow**: Acts as the command center.
- **Quiz Flow Transitions**: Utilizes standard pushes and pops, mostly aligning with the [Push], [Pop], [Replace] principles.

---

## 11. Final Alignment Score

**Score: 95 / 100**

The current implementation strongly represents a **premium Supabase-driven ecosystem** and a **responsive native client**. The backend authority model is respected, and the Android client acts intelligently and optimistically. Minor tweaks to authentication wiring and exact navigation stack popping will bring it to perfect alignment.
