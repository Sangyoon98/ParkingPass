package com.sangyoon.parkingpass.presentation.state

import com.sangyoon.parkingpass.domain.model.ParkingLot

data class ParkingLotUiState(
    val parkingLots: List<ParkingLot> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)