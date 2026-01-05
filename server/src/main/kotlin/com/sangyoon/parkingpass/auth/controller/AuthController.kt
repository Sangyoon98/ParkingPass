package com.sangyoon.parkingpass.auth.controller

import com.sangyoon.parkingpass.auth.dto.AuthResponse
import com.sangyoon.parkingpass.auth.dto.LoginRequest
import com.sangyoon.parkingpass.auth.dto.RegisterRequest
import com.sangyoon.parkingpass.auth.dto.UserResponse
import com.sangyoon.parkingpass.auth.dto.toResponse
import com.sangyoon.parkingpass.auth.service.AuthResult
import com.sangyoon.parkingpass.auth.service.AuthService
import com.sangyoon.parkingpass.common.AuthenticationException
import com.sangyoon.parkingpass.config.KakaoOAuthConfig
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import java.util.UUID

private fun AuthResult.toResponse() =
    AuthResponse(token = token, user = user.toResponse())

fun Route.authController(authService: AuthService) {
    route("/api/v1/auth") {
        post("/register") {
            val request = call.receive<RegisterRequest>()
            val result = authService.register(request)
            call.respond(HttpStatusCode.Created, result.toResponse())
        }

        post("/login") {
            val request = call.receive<LoginRequest>()
            val result = authService.login(request)
            call.respond(HttpStatusCode.OK, result.toResponse())
        }

        post("/oauth/kakao") {
            val request = call.receive<KakaoLoginRequest>()
            val redirectUri = request.redirectUri
                ?: KakaoOAuthConfig.redirectUri()
            val result = authService.loginWithKakao(
                code = request.code,
                redirectUri = redirectUri,
                accessToken = request.accessToken
            )
            call.respond(HttpStatusCode.OK, result.toResponse())
        }

        authenticate("auth-jwt") {
            get("/me") {
                val principal = call.principal<JWTPrincipal>()
                    ?: throw AuthenticationException("인증 정보가 없습니다.")
                val subject = principal.subject
                    ?: throw AuthenticationException("토큰 정보가 올바르지 않습니다.")
                val userId = UUID.fromString(subject)
                val user = authService.getUserById(userId)
                    ?: throw IllegalArgumentException("사용자를 찾을 수 없습니다.")

                call.respond(HttpStatusCode.OK, UserResponse(
                    id = user.id.toString(),
                    email = user.email,
                    name = user.name,
                    createdAt = user.createdAt.toString()
                ))
            }
        }
    }
}

@kotlinx.serialization.Serializable
data class KakaoLoginRequest(
    val code: String? = null,
    val redirectUri: String? = null,
    val accessToken: String? = null
)
