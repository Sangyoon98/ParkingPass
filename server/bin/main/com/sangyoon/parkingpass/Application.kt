package com.sangyoon.parkingpass

import com.sangyoon.parkingpass.health.controller.healthController
import com.sangyoon.parkingpass.parkingevent.controller.parkingEventController
import com.sangyoon.parkingpass.parkingevent.repository.InMemoryParkingEventRepository
import com.sangyoon.parkingpass.parkingevent.sevice.ParkingEventService
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
    val parkingEventRepository = InMemoryParkingEventRepository()
    val parkingEventService = ParkingEventService(parkingEventRepository)

    install(ContentNegotiation) {
        json()
    }

    routing {
        swaggerUI(path = "/swagger-ui", swaggerFile = "openapi/generated.json")
        openAPI("/docs", swaggerFile = "openapi/generated.json")

        healthController()
        parkingEventController(parkingEventService)
    }
}