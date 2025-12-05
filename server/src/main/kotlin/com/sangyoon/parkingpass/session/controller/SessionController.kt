package com.sangyoon.parkingpass.session.controller

import com.sangyoon.parkingpass.session.service.SessionService
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

fun Route.sessionController(
    sessionService: SessionService
) {
    route("/api/v1") {
        /**
         * 현재 주차 중 목록 조회
         *
         * @query parkingLotId 주차장 ID
         * @response 200 application/json List<SessionResponse> 현재 주차 중인 세션 목록
         * @tag Sessions
         */
        get("/sessions/open") {
            val parkingLotId = call.request.queryParameters["parkingLotId"]?.toLongOrNull()
                ?: throw IllegalArgumentException("parkingLotId 파라미터가 필요합니다.")

            val sessions = sessionService.getOpenSessions(parkingLotId)
            call.respond(HttpStatusCode.OK, sessions)
        }

        /**
         * 날짜별 입출차 기록 조회
         *
         * @query parkingLotId 주차장 ID
         * @query date 조회할 날짜 (YYYY-MM-DD 형식, 예: 2025-12-03)
         * @response 200 application/json List<SessionResponse> 해당 날짜의 입출차 기록
         * @tag Sessions
         */
        get("/sessions/history") {
            val parkingLotId = call.request.queryParameters["parkingLotId"]?.toLongOrNull()
                ?: throw IllegalArgumentException("parkingLotId 파라미터가 필요합니다.")

            val date = call.request.queryParameters["date"]
                ?: throw IllegalArgumentException("date 피라미터가 필요합니다. (YYYY-MM-DD 형식)")

            val sessions = sessionService.getSessionHistory(parkingLotId, date)
            call.respond(HttpStatusCode.OK, sessions)
        }
    }
}