package com.sangyoon.parkingpass.common

import kotlinx.serialization.Serializable

/**
 * 공통 에러 응답 형식.
 *
 * @property code 에러 코드 문자열 (예: "GATE_NOT_FOUND", "BAD_REQUEST")
 * @property message 사용자/개발자가 이해할 수 있는 에러 설명
 */
@Serializable
data class ErrorResponse(
    val code: String,
    val message: String
)
