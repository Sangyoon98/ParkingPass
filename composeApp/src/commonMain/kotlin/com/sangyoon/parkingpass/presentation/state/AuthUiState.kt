package com.sangyoon.parkingpass.presentation.state

import com.sangyoon.parkingpass.domain.model.User

data class AuthUiState(
    val email: String = "",
    val name: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentUser: User? = null
)
