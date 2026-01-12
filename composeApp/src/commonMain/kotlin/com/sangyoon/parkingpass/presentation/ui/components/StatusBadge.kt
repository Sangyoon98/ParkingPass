package com.sangyoon.parkingpass.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.sangyoon.parkingpass.presentation.ui.theme.StatusEntry
import com.sangyoon.parkingpass.presentation.ui.theme.StatusExit

enum class SessionStatus {
    ENTRY,
    EXIT
}

@Composable
fun StatusBadge(
    status: SessionStatus,
    timestamp: String,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (status) {
        SessionStatus.ENTRY -> StatusEntry.copy(alpha = 0.15f)
        SessionStatus.EXIT -> StatusExit.copy(alpha = 0.15f)
    }

    val contentColor = when (status) {
        SessionStatus.ENTRY -> StatusEntry
        SessionStatus.EXIT -> StatusExit
    }

    val icon: ImageVector = when (status) {
        SessionStatus.ENTRY -> Icons.Default.ArrowDownward
        SessionStatus.EXIT -> Icons.Default.ArrowUpward
    }

    val label = when (status) {
        SessionStatus.ENTRY -> "입차"
        SessionStatus.EXIT -> "출차"
    }

    Row(
        modifier = modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(16.dp)
        )

        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            modifier = Modifier.padding(start = 4.dp, end = 8.dp)
        )

        Text(
            text = timestamp,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor.copy(alpha = 0.7f)
        )
    }
}
