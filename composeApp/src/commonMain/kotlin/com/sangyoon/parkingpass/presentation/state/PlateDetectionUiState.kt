package com.sangyoon.parkingpass.presentation.state

import com.sangyoon.parkingpass.domain.model.Gate
import com.sangyoon.parkingpass.domain.model.PlateDetectionResult

data class PlateDetectionUiState(
    val gates: List<Gate> = emptyList(),
    val selectedGate: Gate? = null,
    val plateNumber: String = "",
    val isLoading: Boolean = false,
    val result: PlateDetectionResult? = null,
    val error: String? = null
)
