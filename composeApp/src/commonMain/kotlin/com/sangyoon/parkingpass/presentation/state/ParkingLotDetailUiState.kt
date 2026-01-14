package com.sangyoon.parkingpass.presentation.state

import com.sangyoon.parkingpass.domain.model.Gate
import com.sangyoon.parkingpass.domain.model.ParkingLot
import com.sangyoon.parkingpass.domain.model.Session
import com.sangyoon.parkingpass.domain.model.Vehicle

data class ParkingLotDetailUiState(
    val parkingLot: ParkingLot? = null,
    val openSessions: List<Session> = emptyList(),
    val gates: List<Gate> = emptyList(),
    val vehicles: List<Vehicle> = emptyList(),
    val todayEntryCount: Int = 0,
    val todayExitCount: Int = 0,
    val recentActivities: List<Session> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
