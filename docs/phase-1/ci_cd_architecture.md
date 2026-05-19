# CGL Hustle CI/CD Architecture

This document outlines the Continuous Integration and Continuous Delivery (CI/CD) strategy for CGL Hustle, specifically detailing the pipelines, branch protection, and release protocols.

## Branch Protection Rules

To maintain code quality and ensure build stability, the following Branch Protection rules **must be manually configured** in the GitHub repository settings for the `main` branch:

1.  **Require pull request reviews before merging**: Ensures at least one approved review before code is merged.
2.  **Require status checks to pass before merging**:
    *   The `build_and_test` job from the PR Quality Gates workflow (`pr_check.yml`) must be selected as a required status check.
    *   This guarantees that no code can be merged into `main` unless it compiles, passes all lint checks, and passes all unit tests.
3.  **Require branches to be up to date before merging**: Ensures the PR branch is tested against the latest code in `main`.

## Artifact Naming and Versioning Scheme

To provide clear traceability for QA and production releases, all output artifacts (APKs and AABs) are dynamically renamed during the Gradle build process.

The naming convention is strictly:
`cgl-hustle-v{versionName}-{buildType}.[apk|aab]`

**Examples:**
*   Debug Build: `cgl-hustle-v1.0-debug.apk`
*   Release Build: `cgl-hustle-v1.0-release.apk`
*   Release Bundle: `cgl-hustle-v1.0-release.aab`

This renaming is handled automatically within the `app/build.gradle.kts` file using the `applicationVariants` API.

## Workflow Triggers

Our GitHub Actions pipelines are divided into two distinct workflows:

1.  **PR Quality Gates (`pr_check.yml`)**:
    *   **Trigger**: Runs on any `pull_request` targeting the `main` branch.
    *   **Purpose**: Validates the code. Executes `./gradlew clean assembleDebug lint test`. Uploads Lint and Unit Test reports as artifacts for review.

2.  **Merge/Release Workflow (`release.yml`)**:
    *   **Trigger**: Runs on every `push` to the `main` branch.
    *   **Purpose**: Builds the releasable artifacts (APK and AAB). It handles secure signing by decoding environment secrets into a temporary keystore, building the signed outputs, and then securely deleting the keystore.

## Rollback and Re-run Procedure

If a pipeline fails (e.g., the Merge/Release workflow fails to produce an artifact after a merge to `main`), follow these steps:

1.  **Investigate the Failure**:
    *   Check the GitHub Actions summary page. The workflow is configured to output failure details directly to `$GITHUB_STEP_SUMMARY`.
    *   Review the specific job logs to identify if it was a compilation error, a test failure, or an infrastructure issue.
2.  **Re-run the Pipeline (Infrastructure Issue)**:
    *   If the failure was due to a transient issue (e.g., a network timeout downloading dependencies), navigate to the failed run in the "Actions" tab.
    *   Click the **"Re-run all jobs"** or **"Re-run failed jobs"** button in the top right corner.
3.  **Rollback via Code Fix (Code Issue)**:
    *   If the failure was due to a code defect that slipped through, do **not** attempt to force a re-run.
    *   Create a new branch (e.g., `hotfix/fix-release-build`).
    *   Commit the fix, push to GitHub, and open a new Pull Request.
    *   Ensure the PR Quality Gates pass, approve the PR, and merge it to `main`. This merge will automatically trigger a new, healthy release build.
