package com.sangyoon.parkingpass.data.repository

import com.sangyoon.parkingpass.api.dto.LoginRequest
import com.sangyoon.parkingpass.api.dto.RegisterRequest
import com.sangyoon.parkingpass.data.datasource.ParkingApiDataSource
import com.sangyoon.parkingpass.data.storage.SecureStorage
import com.sangyoon.parkingpass.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthRepository(
    private val dataSource: ParkingApiDataSource,
    private val secureStorage: SecureStorage
) {
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _authToken = MutableStateFlow<String?>(null)
    val authToken: StateFlow<String?> = _authToken.asStateFlow()

    suspend fun register(email: String, password: String, name: String?): User {
        val response = dataSource.register(RegisterRequest(email, password, name))
        persistToken(response.token)
        val user = response.user.toDomain()
        _currentUser.value = user
        return user
    }

    suspend fun login(email: String, password: String): User {
        val response = dataSource.login(LoginRequest(email, password))
        persistToken(response.token)
        val user = response.user.toDomain()
        _currentUser.value = user
        return user
    }

    suspend fun loginWithKakao(
        code: String? = null,
        redirectUri: String? = null,
        accessToken: String? = null
    ): User {
        val response = dataSource.loginWithKakao(code, redirectUri, accessToken)
        persistToken(response.token)
        val user = response.user.toDomain()
        _currentUser.value = user
        return user
    }

    suspend fun loadProfile(): User {
        val profile = dataSource.getProfile()
        val user = profile.toDomain()
        _currentUser.value = user
        return user
    }

    suspend fun logout() {
        persistToken(null)
        _currentUser.value = null
    }

    suspend fun restoreSession() {
        val savedToken = try {
            secureStorage.getToken()
        } catch (_: Exception) {
            null
        }
        if (savedToken.isNullOrBlank()) {
            persistToken(null)
            return
        }

        try {
            persistToken(savedToken)
            loadProfile()
        } catch (e: Exception) {
            println("[AuthRepository] Failed to restore session: ${e.message}")
            persistToken(null)
            _currentUser.value = null
        }
    }

    private suspend fun persistToken(token: String?) {
        _authToken.value = token
        dataSource.setAuthToken(token)
        if (token.isNullOrBlank()) {
            secureStorage.clearToken()
        } else {
            secureStorage.saveToken(token)
        }
    }

    private fun com.sangyoon.parkingpass.api.dto.UserResponse.toDomain() = User(
        id = id,
        email = email,
        name = name
    )
}
