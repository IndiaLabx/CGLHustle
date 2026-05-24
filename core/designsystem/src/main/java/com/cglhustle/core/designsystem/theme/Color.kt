package com.cglhustle.core.designsystem.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// CGL Hustle Semantic Color System v1
// Designed for high readability, low fatigue, and professional confidence.
// ============================================================================

// Brand Core (Professional Blue)
val BrandPrimaryLight = Color(0xFF2563EB)
val BrandPrimaryDark = Color(0xFF60A5FA)

// Light Theme Neutrals (Zinc/Slate based for calmness)
val BackgroundLight = Color(0xFFFAFAFA)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceVariantLight = Color(0xFFF4F4F5)
val TextHighEmphasisLight = Color(0xFF18181B)
val TextMediumEmphasisLight = Color(0xFF52525B)
val OutlineLight = Color(0xFFE4E4E7)

// Dark Theme Neutrals
val BackgroundDark = Color(0xFF09090B)
val SurfaceDark = Color(0xFF18181B)
val SurfaceVariantDark = Color(0xFF27272A)
val TextHighEmphasisDark = Color(0xFFFAFAFA)
val TextMediumEmphasisDark = Color(0xFFA1A1AA)
val OutlineDark = Color(0xFF3F3F46)

// Semantic Feedback Roles
val SemanticSuccess = Color(0xFF10B981)
val SemanticSuccessDark = Color(0xFF34D399)
val SemanticWarning = Color(0xFFF59E0B)
val SemanticWarningDark = Color(0xFFFBBF24)
val SemanticError = Color(0xFFEF4444)
val SemanticErrorDark = Color(0xFFF87171)

// Specific Quiz/Study Roles (Exposed for domain-specific UI)
object QuizColors {
    val Correct = SemanticSuccess
    val CorrectDark = SemanticSuccessDark
    val Incorrect = SemanticError
    val IncorrectDark = SemanticErrorDark
    val Warning = SemanticWarning
    val WarningDark = SemanticWarningDark
}
