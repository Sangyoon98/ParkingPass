package com.sangyoon.parkingpass.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangyoon.parkingpass.data.repository.AuthRepository
import com.sangyoon.parkingpass.presentation.state.AuthUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _uiState.update { it.copy(currentUser = user, isLoading = false, error = null) }
            }
        }
    }

    fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun updatePassword(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun logout() {
        authRepository.logout()
        _uiState.update { it.copy(password = "", error = null) }
    }

    fun login() {
        val email = _uiState.value.email
        val password = _uiState.value.password
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                authRepository.login(email, password)
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "로그인에 실패했습니다.") }
            }
        }
    }

    fun register() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                authRepository.register(state.email, state.password, state.name.ifBlank { null })
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "회원가입에 실패했습니다.") }
            }
        }
    }

    fun loadProfile() {
        viewModelScope.launch {
            runCatching {
                authRepository.loadProfile()
            }.onFailure { /* ignore */ }
        }
    }

    fun beginKakaoLogin() {
        if (_uiState.value.isLoading) return
        _uiState.update { it.copy(isLoading = true, error = null) }
    }

    fun onKakaoLoginCancelled() {
        _uiState.update { it.copy(isLoading = false) }
    }

    fun onKakaoAccessToken(accessToken: String) {
        viewModelScope.launch {
            runCatching {
                authRepository.loginWithKakao(accessToken = accessToken)
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "카카오 로그인에 실패했습니다."
                    )
                }
            }
        }
    }

    fun onKakaoLoginError(message: String) {
        _uiState.update {
            it.copy(
                isLoading = false,
                error = message
            )
        }
    }
}
