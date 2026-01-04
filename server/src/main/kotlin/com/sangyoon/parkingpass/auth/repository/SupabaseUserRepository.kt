package com.sangyoon.parkingpass.auth.repository

import com.sangyoon.parkingpass.auth.model.User
import com.sangyoon.parkingpass.common.InstantSerializer
import com.sangyoon.parkingpass.common.UUIDSerializer
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

class SupabaseUserRepository(
    private val supabase: SupabaseClient
) : UserRepository {

    @Serializable
    private data class UserEntity(
        @Serializable(with = UUIDSerializer::class)
        val id: UUID,
        val email: String,
        @SerialName("password_hash")
        val passwordHash: String,
        val name: String? = null,
        @SerialName("created_at")
        @Serializable(with = InstantSerializer::class)
        val createdAt: Instant,
        val provider: String = "local",
        @SerialName("provider_user_id")
        val providerUserId: String? = null
    ) {
        fun toModel() = User(
            id = id,
            email = email,
            name = name,
            createdAt = createdAt
        )
    }

    @Serializable
    private data class UserInsert(
        val email: String,
        @SerialName("password_hash")
        val passwordHash: String,
        val name: String? = null,
        val provider: String = "local",
        @SerialName("provider_user_id")
        val providerUserId: String? = null
    )

    override suspend fun findByEmail(email: String): UserWithPassword? {
        val entity = supabase.from("users")
            .select {
                filter {
                    eq("email", email)
                }
                limit(1)
            }
            .decodeSingleOrNull<UserEntity>()

        return entity?.let { UserWithPassword(it.toModel(), it.passwordHash) }
    }

    override suspend fun findById(id: UUID): User? {
        return supabase.from("users")
            .select {
                filter {
                    eq("id", id)
                }
                limit(1)
            }
            .decodeSingleOrNull<UserEntity>()
            ?.toModel()
    }

    override suspend fun create(email: String, passwordHash: String, name: String?): User {
        val entity = supabase.from("users")
            .insert(
                UserInsert(
                    email = email,
                    passwordHash = passwordHash,
                    name = name
                )
            ) {
                select(Columns.ALL)
            }
            .decodeSingle<UserEntity>()

        return entity.toModel()
    }

    override suspend fun findByProvider(provider: String, providerUserId: String): User? {
        return supabase.from("users")
            .select {
                filter {
                    eq("provider", provider)
                    eq("provider_user_id", providerUserId)
                }
                limit(1)
            }
            .decodeSingleOrNull<UserEntity>()
            ?.toModel()
    }

    override suspend fun createSocialUser(
        provider: String,
        providerUserId: String,
        email: String,
        name: String?,
        passwordHash: String
    ): User {
        val entity = supabase.from("users")
            .insert(
                UserInsert(
                    email = email,
                    passwordHash = passwordHash,
                    name = name,
                    provider = provider,
                    providerUserId = providerUserId
                )
            ) {
                select(Columns.ALL)
            }
            .decodeSingle<UserEntity>()
        return entity.toModel()
    }
}
