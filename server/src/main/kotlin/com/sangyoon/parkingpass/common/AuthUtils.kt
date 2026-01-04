package com.sangyoon.parkingpass.common

import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.principal
import io.ktor.server.auth.jwt.JWTPrincipal
import java.util.UUID

suspend fun ApplicationCall.requireUserId(): UUID {
    val principal = this.principal<JWTPrincipal>()
        ?: throw IllegalStateException("인증이 필요합니다.")
    val subject = principal.subject
        ?: throw IllegalArgumentException("토큰 정보가 올바르지 않습니다.")
    return UUID.fromString(subject)
}
