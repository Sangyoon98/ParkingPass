package com.sangyoon.parkingpass.domain.model

/**
 * 차량 종류 분류
 * UI 레이어에서 아이콘 및 색상 매핑 처리 필요
 */
enum class VehicleCategory(val displayName: String) {
    SEDAN("승용차"),
    SUV("SUV"),
    ELECTRIC("전기차")
}