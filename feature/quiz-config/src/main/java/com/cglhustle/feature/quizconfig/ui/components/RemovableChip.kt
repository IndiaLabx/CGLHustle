package com.cglhustle.feature.quizconfig.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cglhustle.core.ui.theme.AppSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemovableChip(
    label: String,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    InputChip(
        selected = true,
        onClick = { },
        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
        trailingIcon = {
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove $label",
                    modifier = Modifier.size(16.dp)
                )
            }
        },
        colors = InputChipDefaults.inputChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        shape = MaterialTheme.shapes.small,
        border = InputChipDefaults.inputChipBorder(
            borderColor = MaterialTheme.colorScheme.outlineVariant,
            enabled = true,
            selected = true
        ),
        modifier = modifier.padding(end = AppSpacing.xs, bottom = AppSpacing.xs)
    )
}
