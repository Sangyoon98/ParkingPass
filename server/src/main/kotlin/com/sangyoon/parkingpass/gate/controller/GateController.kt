package com.sangyoon.parkingpass.gate.controller

import com.sangyoon.parkingpass.common.AuthMiddleware
import com.sangyoon.parkingpass.parking.model.MemberRole
import com.sangyoon.parkingpass.gate.dto.RegisterGateRequest
import com.sangyoon.parkingpass.gate.service.GateService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.gateController(
    gateService: GateService,
    authMiddleware: AuthMiddleware
) {
    authenticate("auth-jwt") {
        route("/api/v1") {
            /**
             * 게이트 목록 조회 (ADMIN 이상)
             */
            get("/gates") {
                val parkingLotId = call.request.queryParameters["parkingLotId"]?.toLongOrNull()
                    ?: throw IllegalArgumentException("parkingLotId 파라미터가 필요합니다.")

                authMiddleware.requireParkingLotAccess(call, parkingLotId, MemberRole.ADMIN)

                val gates = gateService.getGates(parkingLotId)
                call.respond(HttpStatusCode.OK, gates)
            }

            /**
             * 게이트 등록 (ADMIN 이상)
             */
            post("/gates/register") {
                val request = call.receive<RegisterGateRequest>()
                authMiddleware.requireParkingLotAccess(call, request.parkingLotId, MemberRole.ADMIN)
                val response = gateService.registerGate(request)
                call.respond(HttpStatusCode.Created, response)
            }
        }
    }
}
