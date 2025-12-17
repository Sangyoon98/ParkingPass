package com.sangyoon.parkingpass.presentation.state

import com.sangyoon.parkingpass.domain.model.Vehicle

data class VehicleUiState(
    val vehicles: List<Vehicle> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)