package com.sangyoon.parkingpass.presentation.state

import com.sangyoon.parkingpass.domain.model.ParkingLot

data class ParkingLotSearchUiState(
    val query: String = "",
    val results: List<ParkingLot> = emptyList(),
    val isLoading: Boolean = false,
    val message: String? = null,
    val error: String? = null
)
