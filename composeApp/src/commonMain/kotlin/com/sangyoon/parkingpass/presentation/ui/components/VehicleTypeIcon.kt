package com.sangyoon.parkingpass.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sangyoon.parkingpass.domain.model.VehicleCategory

@Composable
fun VehicleTypeIcon(
    category: VehicleCategory,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .background(
                color = category.color.copy(alpha = 0.15f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = category.icon,
            contentDescription = category.displayName,
            tint = category.color,
            modifier = Modifier.size(size * 0.5f)
        )
    }
}
