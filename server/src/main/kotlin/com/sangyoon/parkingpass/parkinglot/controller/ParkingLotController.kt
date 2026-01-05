package com.sangyoon.parkingpass.parkinglot.controller

import com.sangyoon.parkingpass.parkinglot.dto.CreateParkingLotRequest
import com.sangyoon.parkingpass.parkinglot.service.ParkingLotService
import com.sangyoon.parkingpass.common.requireUserId
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
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
        authenticate("auth-jwt") {
            /**
             * 주차장 등록 (인증 필요)
             */
            post("/parking-lots") {
                val userId = call.requireUserId()
                val request = call.receive<CreateParkingLotRequest>()
                val response = parkingLotService.createParkingLot(userId, request)
                call.respond(HttpStatusCode.Created, response)
            }

            /**
             * 내가 속한 주차장 목록
             */
            get("/parking-lots/my-lots") {
                val userId = call.requireUserId()
                val lots = parkingLotService.getMyParkingLots(userId)
                call.respond(HttpStatusCode.OK, lots)
            }
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
         * 공개 주차장 검색
         *
         * @query [String] q 검색어
         */
        get("/parking-lots/search") {
            val query = call.request.queryParameters["q"] ?: ""
            val lots = parkingLotService.searchPublicLots(query)
            call.respond(HttpStatusCode.OK, lots)
        }

        /**
         * 주차장 조회
         *
         * @path [Long] id 주차장 ID
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
