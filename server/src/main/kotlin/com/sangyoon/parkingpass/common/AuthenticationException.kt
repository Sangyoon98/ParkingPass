package com.sangyoon.parkingpass.common

/**
 * 인증 실패 시 사용되는 예외.
 */
class AuthenticationException(
    override val message: String = "인증이 필요합니다."
) : RuntimeException(message)
