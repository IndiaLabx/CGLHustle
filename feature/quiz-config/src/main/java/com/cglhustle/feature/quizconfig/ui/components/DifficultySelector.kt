package com.cglhustle.feature.quizconfig.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cglhustle.engine.facetedsearch.FilterChipState
import kotlinx.collections.immutable.ImmutableList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DifficultySelector(
    difficultiesState: ImmutableList<FilterChipState>,
    onDifficultySelected: (String) -> Unit
) {
    if (difficultiesState.none { it.isVisible }) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Difficulty",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            difficultiesState.forEach { chipState ->
                if (chipState.isVisible) {
                    FilterChip(
                        selected = chipState.isSelected,
                        onClick = { onDifficultySelected(chipState.name) },
                        label = {
                            Text("${chipState.name} (${chipState.count})")
                        }
                    )
                }
            }
        }
    }
}
