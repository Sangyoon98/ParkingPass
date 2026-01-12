package com.sangyoon.parkingpass.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sangyoon.parkingpass.domain.model.VehicleCategory
import com.sangyoon.parkingpass.presentation.ui.theme.StatusEntry
import com.sangyoon.parkingpass.presentation.ui.theme.StatusExit
import com.sangyoon.parkingpass.presentation.ui.theme.TextSecondary

@Composable
fun ActivityTimelineItem(
    plateNumber: String,
    isEntry: Boolean,
    timestamp: String,
    vehicleCategory: VehicleCategory?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Entry/Exit Icon
        Icon(
            imageVector = if (isEntry) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
            contentDescription = if (isEntry) "입차" else "출차",
            tint = if (isEntry) StatusEntry else StatusExit,
            modifier = Modifier.size(24.dp)
        )

        // Vehicle Type Icon
        if (vehicleCategory != null) {
            VehicleTypeIcon(
                category = vehicleCategory,
                size = 32.dp
            )
        }

        // Plate Number and Info
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = plateNumber,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEntry) "입차" else "출차",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                if (vehicleCategory != null) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Text(
                        text = vehicleCategory.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        // Timestamp
        Text(
            text = timestamp,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}
