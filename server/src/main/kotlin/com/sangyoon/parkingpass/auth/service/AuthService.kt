package com.sangyoon.parkingpass.auth.service

import com.sangyoon.parkingpass.auth.dto.LoginRequest
import com.sangyoon.parkingpass.auth.dto.RegisterRequest
import com.sangyoon.parkingpass.auth.model.User
import com.sangyoon.parkingpass.auth.oauth.KakaoOAuthClient
import com.sangyoon.parkingpass.auth.repository.UserRepository
import com.sangyoon.parkingpass.config.JwtConfig
import org.mindrot.jbcrypt.BCrypt
import java.util.Locale
import java.util.UUID

data class AuthResult(
    val token: String,
    val user: User
)

class AuthService(
    private val userRepository: UserRepository,
    private val kakaoOAuthClient: KakaoOAuthClient
) {

    suspend fun register(request: RegisterRequest): AuthResult {
        val email = request.email.trim().lowercase(Locale.getDefault())
        val password = request.password

        require(email.isNotBlank()) { "이메일을 입력해주세요." }
        require(password.length >= 8) { "비밀번호는 8자 이상이어야 합니다." }

        val existing = userRepository.findByEmail(email)
        if (existing != null) {
            throw IllegalArgumentException("이미 등록된 이메일입니다.")
        }

        val hashed = BCrypt.hashpw(password, BCrypt.gensalt())
        val user = userRepository.create(
            email = email,
            passwordHash = hashed,
            name = request.name
        )

        val token = JwtConfig.generateToken(user.id, user.email)
        return AuthResult(token, user)
    }

    suspend fun login(request: LoginRequest): AuthResult {
        val email = request.email.trim().lowercase(Locale.getDefault())
        val password = request.password

        val userWithPassword = userRepository.findByEmail(email)
            ?: throw IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.")

        if (!BCrypt.checkpw(password, userWithPassword.passwordHash)) {
            throw IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.")
        }

        val user = userWithPassword.user
        val token = JwtConfig.generateToken(user.id, user.email)
        return AuthResult(token, user)
    }

    suspend fun getUserById(userId: UUID): User? = userRepository.findById(userId)

    suspend fun loginWithKakao(
        code: String?,
        redirectUri: String?,
        accessToken: String?
    ): AuthResult {
        val oauthAccessToken = when {
            !accessToken.isNullOrBlank() -> accessToken
            !code.isNullOrBlank() -> {
                val safeRedirect = redirectUri ?: KakaoOAuthClient.DEFAULT_REDIRECT_URI
                val tokenResponse = kakaoOAuthClient.exchangeCode(code, safeRedirect)
                tokenResponse.accessToken
            }
            else -> throw IllegalArgumentException("카카오 로그인 정보가 올바르지 않습니다.")
        }

        val kakaoUser = kakaoOAuthClient.fetchUser(oauthAccessToken)
        val provider = "kakao"
        val providerUserId = kakaoUser.id.toString()

        val existingUser = userRepository.findByProvider(provider, providerUserId)
        val user = existingUser ?: run {
            val email = kakaoUser.account?.email
                ?: "kakao_${providerUserId}@kakao-user.local"
            val nickname = kakaoUser.account?.profile?.nickname

            val existingByEmail = userRepository.findByEmail(email)
            if (existingByEmail != null) {
                throw IllegalArgumentException("이미 해당 이메일로 가입된 계정이 있습니다. 기존 계정으로 로그인해주세요.")
            }

            val placeholderPassword = BCrypt.hashpw(UUID.randomUUID().toString(), BCrypt.gensalt())
            userRepository.createSocialUser(
                provider = provider,
                providerUserId = providerUserId,
                email = email,
                name = nickname,
                passwordHash = placeholderPassword
            )
        }

        val jwt = JwtConfig.generateToken(user.id, user.email)
        return AuthResult(jwt, user)
    }
}
