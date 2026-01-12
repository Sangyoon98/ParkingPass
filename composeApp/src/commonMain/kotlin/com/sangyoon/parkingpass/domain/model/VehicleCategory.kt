package com.sangyoon.parkingpass.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricCar
import androidx.compose.ui.graphics.vector.ImageVector
import com.sangyoon.parkingpass.presentation.ui.theme.VehicleElectric
import com.sangyoon.parkingpass.presentation.ui.theme.VehicleSUV
import com.sangyoon.parkingpass.presentation.ui.theme.VehicleSedan

enum class VehicleCategory(
    val displayName: String,
    val icon: ImageVector,
    val color: androidx.compose.ui.graphics.Color
) {
    SEDAN("승용차", Icons.Default.DirectionsCar, VehicleSedan),
    SUV("SUV", Icons.Default.DirectionsCar, VehicleSUV),
    ELECTRIC("전기차", Icons.Default.ElectricCar, VehicleElectric)
}