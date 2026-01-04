package com.sangyoon.parkingpass.data.repository

import com.sangyoon.parkingpass.api.dto.LoginRequest
import com.sangyoon.parkingpass.api.dto.RegisterRequest
import com.sangyoon.parkingpass.data.datasource.ParkingApiDataSource
import com.sangyoon.parkingpass.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthRepository(
    private val dataSource: ParkingApiDataSource
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

    suspend fun loginWithKakao(code: String, redirectUri: String?): User {
        val response = dataSource.loginWithKakao(code, redirectUri)
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

    fun logout() {
        persistToken(null)
        _currentUser.value = null
    }

    private fun persistToken(token: String?) {
        _authToken.value = token
        dataSource.setAuthToken(token)
    }

    private fun com.sangyoon.parkingpass.api.dto.UserResponse.toDomain() = User(
        id = id,
        email = email,
        name = name
    )
}
