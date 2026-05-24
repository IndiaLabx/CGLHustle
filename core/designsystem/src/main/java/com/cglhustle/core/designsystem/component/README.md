# CGL Hustle Design System Components

This package contains strict, reusable Compose UI components conforming to the native Material 3 design system rules for CGL Hustle.

Components here should NEVER:
- Rely on feature-specific domain models.
- Perform side-effects natively (like triggering navigation or hitting a repository).
- Hardcode paddings outside of the `AppSpacing` token scale.

All components MUST:
- Accept a `Modifier` as their first optional parameter.
- Rely on `MaterialTheme.colorScheme` and `MaterialTheme.typography`.
