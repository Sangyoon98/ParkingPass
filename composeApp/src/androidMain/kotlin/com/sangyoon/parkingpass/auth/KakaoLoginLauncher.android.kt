package com.sangyoon.parkingpass.auth

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@Composable
actual fun rememberKakaoLoginLauncher(): KakaoLoginLauncher {
    val context = LocalContext.current
    val activity = context.findActivity()
        ?: throw IllegalStateException("Kakao login requires a ComponentActivity context.")
    return remember(activity) {
        AndroidKakaoLoginLauncher(activity)
    }
}

private class AndroidKakaoLoginLauncher(
    private val activity: ComponentActivity
) : KakaoLoginLauncher {
    override suspend fun launch(): KakaoLoginResult = suspendCancellableCoroutine { continuation ->
        val callback = fun(token: OAuthToken?, error: Throwable?) {
            if (continuation.isCompleted) return
            when {
                error != null -> {
                    val cause = if (error is ClientError) error.reason else null
                    when (cause) {
                        ClientErrorCause.Cancelled -> continuation.resume(KakaoLoginResult.Cancelled)
                        else -> continuation.resume(
                            KakaoLoginResult.Failure(
                                error.message ?: "카카오 로그인 중 오류가 발생했습니다."
                            )
                        )
                    }
                }
                token != null -> {
                    continuation.resume(KakaoLoginResult.Success(token.accessToken))
                }
                else -> continuation.resume(KakaoLoginResult.Failure("카카오 로그인에 실패했습니다."))
            }
        }

        val userApiClient = UserApiClient.instance
        if (userApiClient.isKakaoTalkLoginAvailable(activity)) {
            userApiClient.loginWithKakaoTalk(activity, callback = callback)
        } else {
            userApiClient.loginWithKakaoAccount(activity, callback = callback)
        }
    }
}

private tailrec fun Context.findActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is android.content.ContextWrapper -> baseContext.findActivity()
    else -> null
}
