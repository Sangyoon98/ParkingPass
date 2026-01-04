package com.sangyoon.parkingpass.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.sangyoon.parkingpass.auth.bridge.KakaoLoginBridge
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@Composable
actual fun rememberKakaoLoginLauncher(): KakaoLoginLauncher {
    return remember { IOSKakaoLoginLauncher() }
}

@OptIn(ExperimentalForeignApi::class)
private class IOSKakaoLoginLauncher : KakaoLoginLauncher {
    override suspend fun launch(): KakaoLoginResult = suspendCancellableCoroutine { continuation ->
        KakaoLoginBridge.loginWithCompletion { token, cancelled, errorMessage ->
            if (continuation.isCompleted) return@loginWithCompletion
            when {
                token != null -> continuation.resume(KakaoLoginResult.Success(token.toString()))
                cancelled -> continuation.resume(KakaoLoginResult.Cancelled)
                else -> continuation.resume(
                    KakaoLoginResult.Failure(
                        errorMessage?.toString() ?: "카카오 로그인에 실패했습니다."
                    )
                )
            }
        }
    }
}
