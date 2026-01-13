package com.sangyoon.parkingpass.presentation.state

import com.sangyoon.parkingpass.domain.model.ParkingLot

enum class ParkingLotFilter {
    ALL,        // 전체
    OPERATING,  // 운영중
    FULL        // 만차
}

data class ParkingLotUiState(
    val myParkingLots: List<ParkingLot> = emptyList(),
    val publicParkingLots: List<ParkingLot> = emptyList(),
    val selectedFilter: ParkingLotFilter = ParkingLotFilter.ALL,
    val creationSuccess: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)
