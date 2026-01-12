package com.sangyoon.parkingpass.presentation.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sangyoon.parkingpass.presentation.ui.theme.PrimaryBlue
import com.sangyoon.parkingpass.presentation.ui.theme.TextSecondary

@Composable
fun <T> FilterChipRow(
    filters: List<T>,
    selectedFilter: T,
    onFilterSelected: (T) -> Unit,
    filterLabel: (T) -> String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filters.forEach { filter ->
            val isSelected = filter == selectedFilter

            FilterChip(
                selected = isSelected,
                onClick = { onFilterSelected(filter) },
                label = {
                    Text(
                        text = filterLabel(filter),
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = TextSecondary,
                    selectedContainerColor = PrimaryBlue.copy(alpha = 0.2f),
                    selectedLabelColor = PrimaryBlue
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = MaterialTheme.colorScheme.outline,
                    selectedBorderColor = PrimaryBlue,
                    enabled = true,
                    selected = isSelected
                )
            )
        }
    }
}
