# UI State Contract

This document outlines the standard UI state management contract for the CGL Hustle application. It ensures consistent handling of loading, success, and error states across all features, providing a uniform experience for the user.

## Core States

All feature UI state objects should be sealed interfaces or classes, with `com.cglhustle.core.ui.state.UiState<T>` providing the foundation:

```kotlin
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<out T>(val data: T, val transientError: AppError? = null) : UiState<T>
    data class Error(val error: AppError) : UiState<Nothing>
}
```

### 1. Loading
Represents the initial state before any data has been fetched, or when a full-screen blocking load is taking place. The UI should display a prominent loading indicator (e.g., a centered `CircularProgressIndicator`).

### 2. Error (Blocking)
Represents a failure to load the *initial* requisite data for the screen. The UI should display a full-screen error state, typically with a description of the error and a "Retry" button.

### 3. Success
Represents the state where the primary data for the screen is available and can be displayed.
`val data: T` contains the domain model required for the UI.

## Mutation Failure UX (Transient Errors)
When a user interaction triggers a mutation (e.g., submitting a form, toggling a favorite) while the screen is already in the `Success` state, a failure should *not* transition the entire screen back to the blocking `Error` state. Doing so would destroy the user's context and data.

Instead, mutation errors are handled as **transient errors**:
1. The mutation is attempted.
2. If it fails, the domain/data layer returns an `AppError`.
3. The ViewModel updates the current `UiState.Success` to include this `transientError`.
4. The UI observes the `transientError` and displays it using a non-intrusive mechanism, such as a Snackbar.
5. The `transientError` should be mapped to a user-friendly string using `AppError.toUserFriendlyMessage()`.
6. Once displayed, the UI or ViewModel must clear the `transientError` (set it back to `null`) to prevent the error from being shown again on recomposition.
