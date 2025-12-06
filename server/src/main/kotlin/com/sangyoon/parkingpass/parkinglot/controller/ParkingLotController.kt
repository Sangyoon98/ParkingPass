package com.sangyoon.parkingpass.parkinglot.controller

import com.sangyoon.parkingpass.parkinglot.dto.CreateParkingLotRequest
import com.sangyoon.parkingpass.parkinglot.service.ParkingLotService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.parkingLotController(
    parkingLotService: ParkingLotService
) {
    route("/api/v1") {
        /**
         * 주차장 등록
         *
         * @body application/json CreateParkingLotRequest 주차장 정보
         * @response 201 application/json ParkingLotResponse 생성된 주차장 정보
         * @tag ParkingLots
         */
        post("/parking-lots") {
            val request = call.receive<CreateParkingLotRequest>()
            val response = parkingLotService.createParkingLot(request)
            call.respond(HttpStatusCode.Created, response)
        }

        /**
         * 주차장 목록 조회
         *
         * @response 200 application/json List<ParkingLotResponse> 주차장 목록
         * @tag ParkingLots
         */
        get("/parking-lots") {
            val lots = parkingLotService.getAllParkingLots()
            call.respond(HttpStatusCode.OK, lots)
        }

        /**
         * 주차장 조회
         *
         * @path id 주차장 ID
         * @response 200 application/json ParkingLotResponse 주차장 정보
         * @tag ParkingLots
         */
        get("/parking-lots/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
                ?: throw IllegalArgumentException("주차장 ID가 필요합니다.")
            val lot = parkingLotService.getParkingLot(id)
            call.respond(HttpStatusCode.OK, lot)
        }
    }
}