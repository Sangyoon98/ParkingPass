package com.sangyoon.parkingpass.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricCar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sangyoon.parkingpass.domain.model.VehicleCategory

@Composable
fun VehicleTypeIcon(
    category: VehicleCategory,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    val (icon, color) = when (category) {
        VehicleCategory.SEDAN -> Icons.Default.DirectionsCar to Color(0xFF2196F3)
        VehicleCategory.SUV -> Icons.Default.DirectionsCar to Color(0xFF4CAF50)
        VehicleCategory.ELECTRIC -> Icons.Default.ElectricCar to Color(0xFFFFEB3B)
    }

    Box(
        modifier = modifier
            .size(size)
            .background(
                color = color.copy(alpha = 0.15f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = category.displayName,
            tint = color,
            modifier = Modifier.size(size * 0.5f)
        )
    }
}
