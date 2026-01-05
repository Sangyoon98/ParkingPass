package com.sangyoon.parkingpass.session.controller

import com.sangyoon.parkingpass.common.AuthMiddleware
import com.sangyoon.parkingpass.parking.model.MemberRole
import com.sangyoon.parkingpass.session.service.SessionService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

fun Route.sessionController(
    sessionService: SessionService,
    authMiddleware: AuthMiddleware
) {
    authenticate("auth-jwt") {
        route("/api/v1") {
            /**
             * 현재 주차 중 목록 조회 (ADMIN 이상)
             */
            get("/sessions/open") {
                val parkingLotId = call.request.queryParameters["parkingLotId"]?.toLongOrNull()
                    ?: throw IllegalArgumentException("parkingLotId 파라미터가 필요합니다.")
                authMiddleware.requireParkingLotAccess(call, parkingLotId, MemberRole.ADMIN)

                val sessions = sessionService.getOpenSessions(parkingLotId)
                call.respond(HttpStatusCode.OK, sessions)
            }

            /**
             * 날짜별 입출차 기록 조회 (ADMIN 이상)
             */
            get("/sessions/history") {
                val parkingLotId = call.request.queryParameters["parkingLotId"]?.toLongOrNull()
                    ?: throw IllegalArgumentException("parkingLotId 파라미터가 필요합니다.")

                authMiddleware.requireParkingLotAccess(call, parkingLotId, MemberRole.ADMIN)

                val date = call.request.queryParameters["date"]
                    ?: throw IllegalArgumentException("date 피라미터가 필요합니다. (YYYY-MM-DD 형식)")

                val sessions = sessionService.getSessionHistory(parkingLotId, date)
                call.respond(HttpStatusCode.OK, sessions)
            }

            /**
             * 번호판으로 현재 세션 조회 (멤버)
             */
            get("/parking-lots/{parkingLotId}/sessions/current/{plateNumber}") {
                val parkingLotId = call.parameters["parkingLotId"]?.toLongOrNull()
                    ?: throw IllegalArgumentException("parkingLotId 파라미터가 필요합니다.")
                val plateNumber = call.parameters["plateNumber"]
                    ?: throw IllegalArgumentException("plateNumber 파라미터가 필요합니다.")

                authMiddleware.requireParkingLotAccess(call, parkingLotId, MemberRole.MEMBER)

                val session = sessionService.getCurrentSessionByPlateNumber(parkingLotId, plateNumber)
                if (session != null) {
                    call.respond(HttpStatusCode.OK, session)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
        }
    }
}
