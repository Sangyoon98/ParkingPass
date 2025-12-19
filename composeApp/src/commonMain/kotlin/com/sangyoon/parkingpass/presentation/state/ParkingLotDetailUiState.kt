package com.sangyoon.parkingpass.presentation.state

import com.sangyoon.parkingpass.domain.model.ParkingLot
import com.sangyoon.parkingpass.domain.model.Session

data class ParkingLotDetailUiState(
    val parkingLot: ParkingLot? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
