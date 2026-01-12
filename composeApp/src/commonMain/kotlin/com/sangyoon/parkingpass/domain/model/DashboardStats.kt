package com.sangyoon.parkingpass.domain.model

enum class Trend {
    UP,
    DOWN,
    NEUTRAL
}

data class DashboardStats(
    val currentOccupancy: Int,
    val totalCapacity: Int,
    val todayEntries: Int,
    val todayExits: Int,
    val entryTrend: Trend = Trend.NEUTRAL,
    val exitTrend: Trend = Trend.NEUTRAL,
    val gateCount: Int = 0,
    val vehicleCount: Int = 0
)
