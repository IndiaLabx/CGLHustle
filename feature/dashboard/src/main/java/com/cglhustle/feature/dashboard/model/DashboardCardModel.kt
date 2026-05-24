package com.cglhustle.feature.dashboard.model

import androidx.compose.ui.graphics.vector.ImageVector

data class DashboardCardModel(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val route: String? = null
)
