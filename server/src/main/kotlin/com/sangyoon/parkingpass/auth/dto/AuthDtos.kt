package com.sangyoon.parkingpass.auth.dto

import com.sangyoon.parkingpass.auth.model.User
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String? = null
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val token: String,
    val user: UserResponse
)

@Serializable
data class UserResponse(
    val id: String,
    val email: String,
    val name: String?,
    @SerialName("created_at")
    val createdAt: String
)

fun User.toResponse() = UserResponse(
    id = id.toString(),
    email = email,
    name = name,
    createdAt = createdAt.toString()
)
