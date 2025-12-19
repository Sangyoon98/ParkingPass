package com.sangyoon.parkingpass.presentation.state

import com.sangyoon.parkingpass.domain.model.Session

data class SessionUiState(
    val openSessions: List<Session> = emptyList(),
    val history: List<Session> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedDate: String? = null
)
