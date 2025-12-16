package com.sangyoon.parkingpass.presentation.state

import com.sangyoon.parkingpass.domain.model.Gate

data class GateUiState(
    val gates: List<Gate> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)