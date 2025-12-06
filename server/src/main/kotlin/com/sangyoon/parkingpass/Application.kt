package com.sangyoon.parkingpass

import com.sangyoon.parkingpass.common.configureStatusPages
import com.sangyoon.parkingpass.gate.controller.gateController
import com.sangyoon.parkingpass.gate.service.GateService
import com.sangyoon.parkingpass.health.controller.healthController
import com.sangyoon.parkingpass.parking.model.GateDevice
import com.sangyoon.parkingpass.parking.model.GateDirection
import com.sangyoon.parkingpass.parking.model.Vehicle
import com.sangyoon.parkingpass.parking.model.VehicleCategory
import com.sangyoon.parkingpass.parking.repository.ImMemoryParkingLotRepository
import com.sangyoon.parkingpass.parking.repository.InMemoryGateDeviceRepository
import com.sangyoon.parkingpass.parking.repository.InMemoryParkingSessionRepository
import com.sangyoon.parkingpass.parking.repository.InMemoryVehicleRepository
import com.sangyoon.parkingpass.parkingevent.controller.parkingEventController
import com.sangyoon.parkingpass.parkingevent.repository.InMemoryParkingEventRepository
import com.sangyoon.parkingpass.parkingevent.sevice.ParkingEventService
import com.sangyoon.parkingpass.parkinglot.controller.parkingLotController
import com.sangyoon.parkingpass.parkinglot.service.ParkingLotService
import com.sangyoon.parkingpass.session.controller.sessionController
import com.sangyoon.parkingpass.session.service.SessionService
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
    // Repository 초기화
    val parkingLotRepository = ImMemoryParkingLotRepository()
    val gateDeviceRepository = InMemoryGateDeviceRepository()
    val vehicleRepository = InMemoryVehicleRepository()
    val parkingSessionRepository = InMemoryParkingSessionRepository()
    val parkingEventRepository = InMemoryParkingEventRepository()

    // Service 초기화
    val parkingLotService = ParkingLotService(parkingLotRepository)
    val gateService = GateService(
        gateDeviceRepository = gateDeviceRepository,
        parkingLotRepository = parkingLotRepository
    )
    val vehicleService = VehicleService(
        vehicleRepository = vehicleRepository,
        parkingLotRepository = parkingLotRepository
    )
    val sessionService = SessionService(
        sessionRepository = parkingSessionRepository,
        vehicleRepository = vehicleRepository
    )
    val parkingEventService = ParkingEventService(
        eventRepository = parkingEventRepository,
        gateDeviceRepository = gateDeviceRepository,
        vehicleRepository = vehicleRepository,
        sessionRepository = parkingSessionRepository
    )

    install(ContentNegotiation) { json() }
    configureStatusPages()

    routing {
        swaggerUI(path = "/swagger-ui", swaggerFile = "openapi/generated.json")
        openAPI("/docs", swaggerFile = "openapi/generated.json")

        healthController()
        parkingLotController(parkingLotService)
        parkingEventController(parkingEventService)
        vehicleController(vehicleService)
        sessionController(sessionService)
        gateController(gateService)
    }
}