# CGL Hustle — Defect Governance & Release Readiness

This document defines the process for triage, SLAs, and release readiness for the CGL Hustle Android application, ensuring compliance with Workstream 8 exit criteria.

## Defect Triage & SLAs

All bugs and defects must be triaged according to the severity rules defined below. Default ownership for all Android application defects resides with the **Android Team**.

### P0 (Blocker)
* **Definition:** A defect that causes a critical failure, crash, data loss, or blocks core functionality (e.g., login, syncing) with no workaround.
* **Triage SLA:** Immediate
* **Resolution ETA:** 24 hours
* **Process Impact:** Halts all active feature development until resolved. No releases can occur with an open P0 defect.

### P1 (Critical)
* **Definition:** A defect that severely impacts functionality or user experience, but a temporary workaround may exist, or it is constrained to a specific sub-feature.
* **Triage SLA:** Within the current sprint
* **Resolution ETA:** 48 hours
* **Process Impact:** Blocks the next release candidate (RC) from going to production. Must be resolved before the release branch is cut or merged.

## Regression Checklist Template

This checklist must be executed against the latest build on the `main` branch before a release candidate is approved.

```markdown
# QA Regression Checklist

**Target Version:** [Version Code / Name]
**Git Commit SHA:** [SHA]
**Date:** [YYYY-MM-DD]
**QA Owner:** [Name]

### General
- [ ] App installs and launches successfully from fresh install.
- [ ] App upgrades successfully from the previous production version.
- [ ] No P0 or P1 defects remain open.

### Authentication
- [ ] Login succeeds with valid credentials.
- [ ] App handles `AppError.AuthExpired` correctly (redirects to auth recovery).
- [ ] Logout clears all sensitive local data.

### Offline & Sync (Reliability Test Matrix)
- [ ] Multi-device conflict resolution handles tie-breakers and UI feedback (`AppError.Conflict`).
- [ ] Duplicate idempotency replays are ignored safely.
- [ ] Offline full-session changes enqueue correctly and sync successfully upon network restoration.
- [ ] Background sync recovery and debouncing behave as expected without unbounded loading.

### UI / State
- [ ] Loading states do not hang indefinitely (timeout mapping works).
- [ ] Error states provide a retry call-to-action for read-only flows.
- [ ] Offline indicator (banner/sheet) displays when the device is disconnected.
```

## QA Release Readiness Note Template

Use this template to provide the final Go/No-Go decision for a release candidate. Attach this as an issue comment or document for the release PR.

```markdown
# QA Release Readiness Note

**Release Candidate:** vX.Y.Z
**Date:** YYYY-MM-DD
**QA Sign-off By:** [Name]

### Summary
[Brief summary of the QA cycle, noting any major areas of focus or stability concerns.]

### QA Artifacts & Evidence
- **Regression Checklist:** [Link to completed checklist]
- **Observability Artifacts:** [Link to Datadog/Sentry dashboard or attached CI logs]
- **Test Matrix Results:** [Link to execution run/spreadsheet]
- **Screenshots/Recordings:** [Attach any relevant evidence for UI verification]

### Known Issues
- [List any deferred P2/P3 defects that are acceptable for this release]

### Final Recommendation
**Decision:** [ GO / NO-GO ]

*(If NO-GO, provide the explicit blocker reason and the required fix.)*
```
