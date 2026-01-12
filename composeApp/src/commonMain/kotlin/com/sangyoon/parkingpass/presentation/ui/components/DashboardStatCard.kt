package com.sangyoon.parkingpass.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.sangyoon.parkingpass.domain.model.Trend
import com.sangyoon.parkingpass.presentation.ui.theme.StatusEntry
import com.sangyoon.parkingpass.presentation.ui.theme.StatusError
import com.sangyoon.parkingpass.presentation.ui.theme.TextSecondary

@Composable
fun DashboardStatCard(
    title: String,
    count: Int,
    icon: ImageVector,
    trend: Trend? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }

        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (trend != null && trend != Trend.NEUTRAL) {
                val trendIcon = when (trend) {
                    Trend.UP -> Icons.Default.TrendingUp
                    Trend.DOWN -> Icons.Default.TrendingDown
                    Trend.NEUTRAL -> Icons.Default.TrendingFlat
                }

                val trendColor = when (trend) {
                    Trend.UP -> StatusEntry
                    Trend.DOWN -> StatusError
                    Trend.NEUTRAL -> TextSecondary
                }

                Icon(
                    imageVector = trendIcon,
                    contentDescription = "트렌드",
                    tint = trendColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
