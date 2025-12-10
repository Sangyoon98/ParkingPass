package com.sangyoon.parkingpass.api

import com.sangyoon.parkingpass.api.dto.CreateParkingLotRequest
import com.sangyoon.parkingpass.api.dto.CreateVehicleRequest
import com.sangyoon.parkingpass.api.dto.GateResponse
import com.sangyoon.parkingpass.api.dto.ParkingLotResponse
import com.sangyoon.parkingpass.api.dto.PlateDetectedRequest
import com.sangyoon.parkingpass.api.dto.PlateDetectedResponse
import com.sangyoon.parkingpass.api.dto.RegisterGateRequest
import com.sangyoon.parkingpass.api.dto.SessionResponse
import com.sangyoon.parkingpass.api.dto.VehicleResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.core.Closeable

/**
 * 주차장 관리 API 클라이언트
 */
class ParkingApiClient(baseUrl: String = "http://localhost:8080") : Closeable {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json()
        }
    }

    private val apiBaseUrl = "$baseUrl/api/v1"

    // 번호판 인식 이벤트 처리
    suspend fun postPlateDetected(request: PlateDetectedRequest): PlateDetectedResponse {
        return client.post("$apiBaseUrl/events/plate-detected") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    // 주차장 등록
    suspend fun createParkingLot(request: CreateParkingLotRequest): ParkingLotResponse {
        return client.post("$apiBaseUrl/parking-lots") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    // 주차장 목록 조회
    suspend fun getParkingLots(): List<ParkingLotResponse> {
        return client.get("$apiBaseUrl/parking-lots").body()
    }

    // 주차장 조회
    suspend fun getParkingLot(id: Long): ParkingLotResponse {
        return client.get("$apiBaseUrl/parking-lots/$id").body()
    }

    // 게이트 등록
    suspend fun registerGate(request: RegisterGateRequest): GateResponse {
        return client.post("$apiBaseUrl/gates/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    // 게이트 목록 조회
    suspend fun getGates(parkingLotId: Long): List<GateResponse> {
        return client.get("$apiBaseUrl/gates") {
            parameter("parkingLotId", parkingLotId)
        }.body()
    }

    // 차량 등록
    suspend fun createVehicle(request: CreateVehicleRequest): VehicleResponse {
        return client.post("$apiBaseUrl/vehicles") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    // 차량 목록 조회
    suspend fun getVehicles(parkingLotId: Long): List<VehicleResponse> {
        return client.get("$apiBaseUrl/vehicles") {
            parameter("parkingLotId", parkingLotId)
        }.body()
    }

    // 현재 주차 중 목록
    suspend fun getOpenSessions(parkingLotId: Long): List<SessionResponse> {
        return client.get("$apiBaseUrl/sessions/open") {
            parameter("parkingLotId", parkingLotId)
        }.body()
    }

    // 날짜별 입출차 기록
    suspend fun getSessionHistory(parkingLotId: Long, date: String): List<SessionResponse> {
        return client.get("$apiBaseUrl/sessions/history") {
            parameter("parkingLotId", parkingLotId)
            parameter("date", date)
        }.body()
    }

    override fun close() {
        client.close()
    }
}