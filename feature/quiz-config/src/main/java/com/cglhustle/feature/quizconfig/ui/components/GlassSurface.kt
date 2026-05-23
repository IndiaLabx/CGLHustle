package com.cglhustle.feature.quizconfig.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    backgroundColor: Color = Color.Transparent,
    content: @Composable () -> Unit
) {
    // Pseudo glass implementation using Tonal Layering
    // Removed raw Color.White borders and heavy alpha rendering.
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                if (backgroundColor == Color.Transparent)
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                else backgroundColor
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = shape
            )
    ) {
        content()
    }
}
