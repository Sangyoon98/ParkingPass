package com.sangyoon.parkingpass.data.storage

/**
 * 단말기에 인증 토큰을 안전하게 저장/복원하기 위한 추상화.
 */
interface SecureStorage {
    suspend fun saveToken(token: String)
    suspend fun getToken(): String?
    suspend fun clearToken()
}

expect fun createSecureStorage(): SecureStorage
