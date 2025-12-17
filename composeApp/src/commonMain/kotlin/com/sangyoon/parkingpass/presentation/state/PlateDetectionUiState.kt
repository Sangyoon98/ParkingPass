package com.sangyoon.parkingpass.presentation.state

import com.sangyoon.parkingpass.api.dto.PlateDetectedResponse
import com.sangyoon.parkingpass.domain.model.Gate

data class PlateDetectionUiState(
    val gates: List<Gate> = emptyList(),
    val selectedGate: Gate? = null,
    val plateNumber: String = "",
    val isLoading: Boolean = false,
    val result: PlateDetectedResponse? = null,
    val error: String? = null
)
