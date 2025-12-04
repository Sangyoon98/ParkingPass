package com.sangyoon.parkingpass.common

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

/**
 * Ktor StatusPages 플러그인 설정
 * 모든 예외 처리 로직을 여기에 모음
 */
fun Application.configureStatusPages() {
    install(StatusPages) {
        // 404: 게이트를 찾을 수 없음
        exception<GateNotFoundException> { call, cause ->
            call.respond(
                status = HttpStatusCode.NotFound,
                message = ErrorResponse(
                    code = "GATE_NOT_FOUND",
                    message = "등록되지 않은 게이트(deviceKey)입니다: ${cause.deviceKey}"
                )
            )
        }

        // 400: 잘못된 요청
        exception<IllegalArgumentException> { call, cause ->
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = ErrorResponse(
                    code = "BAD_REQUEST",
                    message = cause.message ?: "잘못된 요청입니다."
                )
            )
        }

        // 500: 서버 내부 오류 (예상치 못한 예외)
        exception<Exception> { call, cause ->
            call.respond(
                status = HttpStatusCode.InternalServerError,
                message = ErrorResponse(
                    code = "INTERNAL_SERVER_ERROR",
                    message = "서버 오류가 발생했습니다: ${cause.message}"
                )
            )
        }
    }
}