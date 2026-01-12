package com.sangyoon.parkingpass.presentation.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.sangyoon.parkingpass.presentation.ui.theme.OccupancyHigh
import com.sangyoon.parkingpass.presentation.ui.theme.OccupancyLow
import com.sangyoon.parkingpass.presentation.ui.theme.OccupancyMedium
import com.sangyoon.parkingpass.presentation.ui.theme.TextSecondary

@Composable
fun OccupancyProgressBar(
    currentOccupancy: Int,
    totalCapacity: Int,
    modifier: Modifier = Modifier
) {
    val percentage = if (totalCapacity > 0) {
        (currentOccupancy.toFloat() / totalCapacity.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    val percentageInt = (percentage * 100).toInt()

    val progressColor = when {
        percentage < 0.7f -> OccupancyLow      // < 70%
        percentage < 0.9f -> OccupancyMedium   // 70-90%
        else -> OccupancyHigh                   // > 90%
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "현재 주차 현황",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = "$currentOccupancy / $totalCapacity",
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            LinearProgressIndicator(
                progress = { percentage },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 60.dp),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round
            )

            Text(
                text = "$percentageInt%",
                style = MaterialTheme.typography.titleMedium,
                color = progressColor,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }

        Text(
            text = "시설 점유율 $percentageInt%",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}
