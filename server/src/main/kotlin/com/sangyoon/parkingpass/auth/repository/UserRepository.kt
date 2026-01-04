package com.sangyoon.parkingpass.auth.repository

import com.sangyoon.parkingpass.auth.model.User
import java.util.UUID

data class UserWithPassword(
    val user: User,
    val passwordHash: String
)

interface UserRepository {
    suspend fun findByEmail(email: String): UserWithPassword?
    suspend fun findById(id: UUID): User?
    suspend fun create(email: String, passwordHash: String, name: String? = null): User
    suspend fun findByProvider(provider: String, providerUserId: String): User?
    suspend fun createSocialUser(
        provider: String,
        providerUserId: String,
        email: String,
        name: String?,
        passwordHash: String = ""
    ): User
}
