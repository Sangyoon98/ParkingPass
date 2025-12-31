package com.sangyoon.parkingpass.gate.controller

import com.sangyoon.parkingpass.gate.dto.RegisterGateRequest
import com.sangyoon.parkingpass.gate.service.GateService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.gateController(
    gateService: GateService
) {
    route("/api/v1") {
        /**
         * 게이트 목록 조회
         *
         * @query [Long] parkingLotId 주차장 ID
         * @response 200 application/json List<GateResponse> 게이트 목록
         * @tag Gates
         */
        get("/gates") {
            val parkingLotId = call.request.queryParameters["parkingLotId"]?.toLongOrNull()
                ?: throw IllegalArgumentException("parkingLotId 파라미터가 필요합니다.")

            val gates = gateService.getGates(parkingLotId)
            call.respond(HttpStatusCode.OK, gates)
        }

        /**
         * 게이트 등록
         *
         * @body application/json RegisterGateRequest 게이트 정보
         * @Response 201 application/json GateResponse 생성된 게이트 정보
         * @tag Gates
         */
        post("/gates/register") {
            val request = call.receive<RegisterGateRequest>()
            val response = gateService.registerGate(request)
            call.respond(HttpStatusCode.Created, response)
        }
    }
}