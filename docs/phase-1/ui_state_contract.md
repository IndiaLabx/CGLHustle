# Phase 1: UI State Contract

This document outlines the strict UI state handling rules for CGL Hustle.

## 1. Unidirectional Data Flow (UDF) & State Architecture

All Compose UI screens must be driven by a single stream of state originating from the ViewModel (typically exposed as a `StateFlow<UiState<T>>`).

The generic `UiState` has exactly three mutually exclusive variants:
*   `Loading`: Indicates initial data fetch. The UI should display a full-screen loading spinner.
*   `Error`: Indicates a full-screen blocking error where data cannot be displayed.
*   `Success`: Contains the rendered data model.

### 1.1 Non-Blocking Mutation Failures

When a UI state is currently `Success` (data is rendered), but a non-blocking background mutation fails (e.g. an outbox update), the data view MUST NOT unmount.

Instead of treating the transient error as a side-effect/one-off event (like a `SharedFlow`), we embrace pure declarative UDF by attaching the transient error directly to the `Success` state:

```kotlin
data class Success<out T>(
    val data: T,
    val transientError: AppError? = null
) : UiState<T>
```

When `transientError` is not null, `StatefulScreenWrapper` reads this value and overlays a Snackbar or non-blocking Banner, while the primary content remains visible and fully interactive. Once the user dismisses the Snackbar or the mutation succeeds, the ViewModel explicitly sets `transientError` back to `null`.

## 2. PII Redaction Strategy

Telemetry and logging payloads MUST NEVER log sensitive user data. See the `StructuredLogger` implementation for specific rules regarding JSON masking of fields like `userId`, `email`, and `token`.

## 3. Error Copy Mapping Guidelines

The `AppError` base interface contains a `telemetryCode` which is intended purely for developer logging (e.g. `ERR_NET_TRANSIENT`).

For user-facing error messages, we map these internal types to human-readable, non-technical English.
*   **Do not output raw HTTP codes or SQL errors** (e.g., avoid "HTTP 503").
*   **Use conversational, empathetic phrasing.** For example: "Our servers are taking a short break. We will save your progress offline."

The logic mapping `AppError` to localized strings is contained within the `AppError.toUserFriendlyMessage(): String` extension.
