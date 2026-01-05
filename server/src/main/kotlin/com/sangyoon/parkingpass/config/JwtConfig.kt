package com.sangyoon.parkingpass.config

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date
import java.util.UUID

object JwtConfig {
    private fun getConfig(key: String, default: String? = null): String {
        return System.getenv(key)
            ?: System.getProperty(key)
            ?: default
            ?: throw IllegalStateException("$key is not set. Configure it via environment variable or JVM property.")
    }

    val secret: String by lazy { getConfig("JWT_SECRET") }
    val issuer: String by lazy { getConfig("JWT_ISSUER", "parkingpass-server") }
    val audience: String by lazy { getConfig("JWT_AUDIENCE", "parkingpass-clients") }
    val realm: String by lazy { getConfig("JWT_REALM", "parkingpass") }
    val expirationMillis: Long by lazy {
        val fallback = 1000L * 60 * 60 * 24 * 7 // 7 days
        System.getenv("JWT_EXPIRATION_MS")?.toLongOrNull()
            ?: System.getProperty("JWT_EXPIRATION_MS")?.toLongOrNull()
            ?: fallback
    }

    val algorithm: Algorithm by lazy { Algorithm.HMAC256(secret) }

    val verifier: JWTVerifier by lazy {
        JWT.require(algorithm)
            .withIssuer(issuer)
            .withAudience(audience)
            .build()
    }

    fun generateToken(userId: UUID, email: String): String {
        val expiresAt = Date(System.currentTimeMillis() + expirationMillis)

        return JWT.create()
            .withSubject(userId.toString())
            .withClaim("email", email)
            .withAudience(audience)
            .withIssuer(issuer)
            .withExpiresAt(expiresAt)
            .sign(algorithm)
    }
}
