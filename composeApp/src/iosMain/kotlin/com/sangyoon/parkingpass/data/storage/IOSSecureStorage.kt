package com.sangyoon.parkingpass.data.storage

import com.sangyoon.parkingpass.security.bridge.SecureStorageBridge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual fun createSecureStorage(): SecureStorage = IOSSecureStorage()

private class IOSSecureStorage : SecureStorage {

    override suspend fun saveToken(token: String) {
        withContext(Dispatchers.Default) {
            if (!SecureStorageBridge.saveToken(token)) {
                throw IllegalStateException("Failed to save token securely.")
            }
        }
    }

    override suspend fun getToken(): String? = withContext(Dispatchers.Default) {
        SecureStorageBridge.loadToken()
    }

    override suspend fun clearToken() {
        withContext(Dispatchers.Default) {
            SecureStorageBridge.clearToken()
        }
    }
}
