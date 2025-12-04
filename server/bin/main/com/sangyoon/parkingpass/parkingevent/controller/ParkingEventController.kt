package com.sangyoon.parkingpass.parkingevent.controller

import com.sangyoon.parkingpass.parkingevent.dto.PlateDetectedRequest
import com.sangyoon.parkingpass.parkingevent.sevice.ParkingEventService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import java.time.Instant

fun Route.parkingEventController(
    parkingEventService: ParkingEventService
) {
    route("/api/v1") {
        /**
         * 번호판 인식 이벤트 처리
         *
         * @body application/json PlateDetectedRequest 번호판 인식 요청
         * @response 200 application/json PlateDetectedResponse 성공 응답
         * @tag ParkingEvents
         */
        post("/events/plate-detected") {
            val request = call.receive<PlateDetectedRequest>()

            // capturedAt이 없으면 서버 현재 시간 사용
            val capturedAt = request.capturedAt?.let { Instant.parse(it) } ?: Instant.now()

            val response = parkingEventService.handlePlateDetected(
                deviceKey = request.deviceKey,
                plateNumber = request.plateNumber,
                capturedAt = capturedAt
            )

            call.respond(HttpStatusCode.OK, response)
        }
    }
}