package com.sangyoon.parkingpass.vehicle.controller

import com.sangyoon.parkingpass.common.AuthMiddleware
import com.sangyoon.parkingpass.parking.model.MemberRole
import com.sangyoon.parkingpass.vehicle.dto.CreateVehicleRequest
import com.sangyoon.parkingpass.vehicle.dto.VehicleResponse
import com.sangyoon.parkingpass.vehicle.service.VehicleService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.vehicleController(
    vehicleService: VehicleService,
    authMiddleware: AuthMiddleware
) {
    authenticate("auth-jwt") {
        route("/api/v1") {
            /**
             * 등록 차량 추가 (ADMIN 이상)
             */
            post("/vehicles") {
                val request = call.receive<CreateVehicleRequest>()
                authMiddleware.requireParkingLotAccess(call, request.parkingLotId, MemberRole.ADMIN)
                val response = vehicleService.createVehicle(request)
                call.respond(HttpStatusCode.Created, response)
            }

            /**
             * 등록 차량 목록 조회 (멤버)
             */
            get("/vehicles") {
                val parkingLotId = call.request.queryParameters["parkingLotId"]?.toLongOrNull()
                    ?: throw IllegalArgumentException("parkingLotId 파라미터가 필요합니다.")

                authMiddleware.requireParkingLotAccess(call, parkingLotId, MemberRole.MEMBER)

                val vehicles = vehicleService.getVehicles(parkingLotId)
                call.respond(HttpStatusCode.OK, vehicles)
            }

            /**
             * 번호판으로 차량 조회 (멤버)
             */
            get("/parking-lots/{parkingLotId}/vehicles/plate/{plateNumber}") {
                val parkingLotId = call.parameters["parkingLotId"]?.toLongOrNull()
                    ?: throw IllegalArgumentException("parkingLotId 파라미터가 필요합니다.")
                val plateNumber = call.parameters["plateNumber"]
                    ?: throw IllegalArgumentException("plateNumber 파라미터가 필요합니다.")

                authMiddleware.requireParkingLotAccess(call, parkingLotId, MemberRole.MEMBER)

                val vehicle = vehicleService.getVehicleByPlateNumber(parkingLotId, plateNumber)
                if (vehicle != null) {
                    call.respond(HttpStatusCode.OK, vehicle)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
        }
    }
}
