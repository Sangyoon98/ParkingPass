package com.sangyoon.parkingpass.health.controller

import com.sangyoon.parkingpass.Greeting
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

fun Route.healthController() {
    route("/api/v1") {
        /**
         * HealthCheck
         *
         * @body application/json HealthCheck 요청
         * @response 200 application/json HealthCheck 성공 응답
         * @tag Health
         */
        get("/health") {
            call.respondText("Ktor: ${Greeting().greet()}")
        }
    }
}