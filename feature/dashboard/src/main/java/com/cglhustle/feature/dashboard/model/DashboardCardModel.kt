package com.cglhustle.feature.dashboard.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class DashboardCardModel(
    val title: String,
    val subtitle: String,
    val color: Color,
    val icon: ImageVector,
    val route: String?,
    val action: (() -> Unit)? = null
)
