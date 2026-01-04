package com.sangyoon.parkingpass.auth.oauth

import com.sangyoon.parkingpass.config.KakaoOAuthConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.accept
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class KakaoOAuthClient(
    private val httpClient: HttpClient = HttpClient(CIO)
) {

    @Serializable
    data class TokenResponse(
        @SerialName("access_token")
        val accessToken: String,
        @SerialName("token_type")
        val tokenType: String,
        @SerialName("refresh_token")
        val refreshToken: String? = null,
        @SerialName("expires_in")
        val expiresIn: Long? = null
    )

    @Serializable
    data class KakaoProfile(
        val nickname: String? = null,
        val thumbnail_image_url: String? = null,
        val profile_image_url: String? = null
    )

    @Serializable
    data class KakaoAccount(
        val email: String? = null,
        val profile: KakaoProfile? = null
    )

    @Serializable
    data class UserResponse(
        val id: Long,
        @SerialName("kakao_account")
        val account: KakaoAccount? = null
    )

    suspend fun exchangeCode(
        code: String,
        redirectUri: String
    ): TokenResponse {
        val response: HttpResponse = httpClient.post("https://kauth.kakao.com/oauth/token") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(
                FormDataContent(
                    formData {
                        append("grant_type", "authorization_code")
                        append("client_id", KakaoOAuthConfig.clientId)
                        append("code", code)
                        append("redirect_uri", redirectUri)
                        KakaoOAuthConfig.clientSecret?.let { append("client_secret", it) }
                    }
                )
            )
        }
        if (!response.status.isSuccess()) {
            throw IllegalArgumentException("카카오 토큰 발급에 실패했습니다: ${response.status.value}")
        }
        return response.body()
    }

    suspend fun fetchUser(token: String): UserResponse {
        val response = httpClient.get("https://kapi.kakao.com/v2/user/me") {
            header(HttpHeaders.Authorization, "Bearer $token")
            accept(ContentType.Application.Json)
        }
        if (!response.status.isSuccess()) {
            throw IllegalArgumentException("카카오 사용자 정보를 가져오지 못했습니다: ${response.status.value}")
        }
        return response.body()
    }
}
