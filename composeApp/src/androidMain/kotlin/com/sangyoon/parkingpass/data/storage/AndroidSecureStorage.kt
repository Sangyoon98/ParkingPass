package com.sangyoon.parkingpass.data.storage

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.sangyoon.parkingpass.ParkingPassApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val SECURE_PREFS_NAME = "secure_storage"
private const val KEY_AUTH_TOKEN = "auth_token"

actual fun createSecureStorage(): SecureStorage {
    val context = ParkingPassApplication.instance.applicationContext
    return AndroidSecureStorage(context)
}

private class AndroidSecureStorage(
    context: Context
) : SecureStorage {

    private val prefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            SECURE_PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    override suspend fun saveToken(token: String) {
        withContext(Dispatchers.IO) {
            prefs.edit().putString(KEY_AUTH_TOKEN, token).apply()
        }
    }

    override suspend fun getToken(): String? = withContext(Dispatchers.IO) {
        prefs.getString(KEY_AUTH_TOKEN, null)
    }

    override suspend fun clearToken() {
        withContext(Dispatchers.IO) {
            prefs.edit().remove(KEY_AUTH_TOKEN).apply()
        }
    }
}
