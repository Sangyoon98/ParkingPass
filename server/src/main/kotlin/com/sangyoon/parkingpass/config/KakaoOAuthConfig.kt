package com.sangyoon.parkingpass.config

object KakaoOAuthConfig {
    val clientId: String by lazy {
        System.getenv("KAKAO_CLIENT_ID")
            ?: System.getProperty("KAKAO_CLIENT_ID")
            ?: throw IllegalStateException("KAKAO_CLIENT_ID is not set")
    }

    val clientSecret: String? by lazy {
        System.getenv("KAKAO_CLIENT_SECRET")
            ?: System.getProperty("KAKAO_CLIENT_SECRET")
    }

    fun redirectUri(default: String? = null): String {
        return System.getenv("KAKAO_REDIRECT_URI")
            ?: System.getProperty("KAKAO_REDIRECT_URI")
            ?: default
            ?: throw IllegalStateException("KAKAO_REDIRECT_URI is not set")
    }
}
