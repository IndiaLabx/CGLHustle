# CGL Hustle: Multi-Backend Architecture

This document defines the official enterprise-grade dual backend infrastructure for CGL Hustle.

## 1. Backend Responsibility Boundary

The application strictly separates responsibilities into distinct backend environments.
**Repositories must NEVER mix backend responsibilities.**

| System | Backend | Purpose |
|---|---|---|
| Auth | Android CGL Hustle (Primary) | Read & Write |
| Profiles | Android CGL Hustle (Primary) | Read & Write |
| Bookmarks | Android CGL Hustle (Primary) | Read & Write |
| Saved Quizzes | Android CGL Hustle (Primary) | Read & Write |
| Analytics | Android CGL Hustle (Primary) | Read & Write |
| Session Runtime | Android CGL Hustle (Primary) | Read & Write |
| Questions | **GK LLM (Secondary)** | **Read-Only** |

### GK LLM Restriction
The **GK LLM Backend** is strictly a **READ-ONLY Question Content Service**.
It MUST NOT be used for:
- Inserts, Updates, or Deletes
- Authentication or Session Persistence
- Bookmarks or User State
- Runtime App Systems

## 2. Dependency Injection Rules

Generic `SupabaseClient` or `HttpClient` injections are **FORBIDDEN**.
Modules must explicitly request backend dependencies using Qualifiers.

- `@PrimaryBackend SupabaseClient`
- `@QuestionBackend SupabaseClient`
- `@PrimaryBackendHttpClient HttpClient`
- `@QuestionBackendHttpClient HttpClient`

### Authentication Interceptor Isolation
Only the **Primary Backend Client** (`@PrimaryBackendHttpClient`) contains Ktor Auth Interceptors, session headers, access token injection, and authenticated runtime context.
The **Question Backend Client** remains completely clean and isolated.

## 3. Configuration Access & Security
Configuration and Environment variables are encapsulated inside the `core:config` module.
- `EnvironmentProvider` and `BackendConfig` are the single source of truth for `BuildConfig`.
- No other modules (feature, network, repositories) are allowed to access `BuildConfig` directly.
- The app uses **Crash-Early Validation** in `DEBUG` builds to guarantee urls and keys are present.

### Security Logging Rule
NEVER log anon keys, Authorization headers, URLs containing secrets, or session tokens.
Only log structured backend failure states.

## 4. Failure Isolation Expectation
In the event that the **GK LLM Backend** fails, the application **MUST STILL**:
- Authenticate Users
- Load Dashboard
- Load Profile
- Load Bookmarks & Analytics
- Restore Session
Only question fetching degrades.

## 5. Future Scalability Constraints
This architecture is built for horizontal scalability without requiring rewrites of the core infrastructure. As future backends (Edge Functions, Analytics, Vector Search) are added, they should follow the same pattern of modular config boundaries, distinct clients, specific Hilt Qualifiers, and rigid responsibility silos.
