package com.sangyoon.parkingpass.auth

import androidx.compose.runtime.Composable

sealed class KakaoLoginResult {
    data class Success(val accessToken: String) : KakaoLoginResult()
    object Cancelled : KakaoLoginResult()
    data class Failure(val message: String) : KakaoLoginResult()
}

interface KakaoLoginLauncher {
    suspend fun launch(): KakaoLoginResult
}

@Composable
expect fun rememberKakaoLoginLauncher(): KakaoLoginLauncher
