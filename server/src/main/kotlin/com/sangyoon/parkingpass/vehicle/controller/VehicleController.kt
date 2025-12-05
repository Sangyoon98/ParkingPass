package com.sangyoon.parkingpass.vehicle.controller

import com.sangyoon.parkingpass.vehicle.dto.CreateVehicleRequest
import com.sangyoon.parkingpass.vehicle.service.VehicleService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.vehicleController(
    vehicleService: VehicleService
) {
    route("/api/v1") {
        /**
         * 등록 차량 추가
         *
         * @body application/json CreateVehicleRequest 등록 차량 정보
         * @response 200 application/json VehicleResponse 생성된 차량 정보
         * @tag Vehicles
         */
        post("/vehicles") {
            val request = call.receive<CreateVehicleRequest>()
            val response = vehicleService.createVehicle(request)
            call.respond(HttpStatusCode.Created, response)
        }

        /**
         * 등록 차량 목록 조회
         *
         * @query parkingLotId 주차장 ID
         * @response 200 application/json List<VehicleResponse> 차량 목록
         * @tag Vehicles
         */
        get("/vehicles") {
            val parkingLotId = call.request.queryParameters["parkingLotId"]?.toLongOrNull()
                ?: throw IllegalArgumentException("parkingLotId 파라미터가 필요합니다.")

            val vehicles = vehicleService.getVehicles(parkingLotId)
            call.respond(HttpStatusCode.OK, vehicles)
        }
    }
}