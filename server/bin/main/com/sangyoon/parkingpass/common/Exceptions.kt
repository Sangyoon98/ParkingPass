package com.sangyoon.parkingpass.common

import java.lang.RuntimeException

/**
 * 등록되지 않은 게이트 deviceKey로 요청한 경우
 */
class GateNotFoundException(
    val deviceKey: String
) : RuntimeException("Gate not found for deviceKey: $deviceKey")

/**
 * 잘못된 요청 파라미터
 */
class BadRequestException(
    override val message: String
) : RuntimeException(message)