package com.sangyoon.parkingpass.parking.model

import kotlinx.serialization.Serializable

@Serializable
enum class VehicleCategory {
    RESIDENT,
    EMPLOYEE,
    DELIVERY,
    VISITOR
}