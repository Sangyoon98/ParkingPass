package com.sangyoon.parkingpass

import com.sangyoon.parkingpass.common.configureStatusPages
import com.sangyoon.parkingpass.health.controller.healthController
import com.sangyoon.parkingpass.parking.model.GateDevice
import com.sangyoon.parkingpass.parking.model.GateDirection
import com.sangyoon.parkingpass.parking.model.Vehicle
import com.sangyoon.parkingpass.parking.model.VehicleCategory
import com.sangyoon.parkingpass.parking.repository.InMemoryGateDeviceRepository
import com.sangyoon.parkingpass.parking.repository.InMemoryParkingSessionRepository
import com.sangyoon.parkingpass.parking.repository.InMemoryVehicleRepository
import com.sangyoon.parkingpass.parkingevent.controller.parkingEventController
import com.sangyoon.parkingpass.parkingevent.repository.InMemoryParkingEventRepository
import com.sangyoon.parkingpass.parkingevent.sevice.ParkingEventService
import com.sangyoon.parkingpass.vehicle.controller.vehicleController
import com.sangyoon.parkingpass.vehicle.service.VehicleService
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.openapi.openAPI
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.routing.*

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    val gateDeviceRepository = InMemoryGateDeviceRepository()
    val vehicleRepository = InMemoryVehicleRepository()
    val parkingSessionRepository = InMemoryParkingSessionRepository()
    val parkingEventRepository = InMemoryParkingEventRepository()

    // TODO: 초기 데이터(seed) 삽입 (나중에)
    // gateDeviceRepository.save(...)
    // vehicleRepository.save(...)

    // 주차장 1개만 있다고 가정하고 parkingLotId = 1L 로 통일
    val gateEntrance = gateDeviceRepository.save(
        GateDevice(
            id = 0L,
            parkingLotId = 1L,
            name = "서해그랑블 정문",
            deviceKey = "서해그랑블",       // ← 요청에 쓰는 deviceKey랑 같아야 함
            direction = GateDirection.BOTH
        )
    )
    // 예시: 등록 차량 하나
    vehicleRepository.save(
        Vehicle(
            id = 0L,
            parkingLotId = 1L,
            plateNumber = "02우1138",
            label = "206동 1603호",
            category = VehicleCategory.RESIDENT,
            memo = null
        )
    )

    val parkingEventService = ParkingEventService(
        eventRepository = parkingEventRepository,
        gateDeviceRepository = gateDeviceRepository,
        vehicleRepository = vehicleRepository,
        sessionRepository = parkingSessionRepository
    )

    val vehicleService = VehicleService(vehicleRepository)

    install(ContentNegotiation) { json() }

    configureStatusPages()

    routing {
        swaggerUI(path = "/swagger-ui", swaggerFile = "openapi/generated.json")
        openAPI("/docs", swaggerFile = "openapi/generated.json")

        healthController()
        parkingEventController(parkingEventService)
        vehicleController(vehicleService)
    }
}