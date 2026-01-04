package com.sangyoon.parkingpass.api

import com.sangyoon.parkingpass.api.dto.AuthResponse
import com.sangyoon.parkingpass.api.dto.CreateParkingLotRequest
import com.sangyoon.parkingpass.api.dto.CreateVehicleRequest
import com.sangyoon.parkingpass.api.dto.ErrorResponseDto
import com.sangyoon.parkingpass.api.dto.GateResponse
import com.sangyoon.parkingpass.api.dto.InviteMemberRequest
import com.sangyoon.parkingpass.api.dto.KakaoLoginRequest
import com.sangyoon.parkingpass.api.dto.LoginRequest
import com.sangyoon.parkingpass.api.dto.ParkingLotMemberResponse
import com.sangyoon.parkingpass.api.dto.ParkingLotResponse
import com.sangyoon.parkingpass.api.dto.PlateDetectedRequest
import com.sangyoon.parkingpass.api.dto.PlateDetectedResponse
import com.sangyoon.parkingpass.api.dto.RegisterGateRequest
import com.sangyoon.parkingpass.api.dto.RegisterRequest
import com.sangyoon.parkingpass.api.dto.SessionResponse
import com.sangyoon.parkingpass.api.dto.UpdateMemberRoleRequest
import com.sangyoon.parkingpass.api.dto.UserResponse
import com.sangyoon.parkingpass.api.dto.VehicleResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.core.Closeable
import kotlinx.serialization.json.Json

/**
 * 주차장 관리 API 클라이언트
 */
class ParkingApiClient(baseUrl: String = "http://localhost:8080") : Closeable {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    private val apiBaseUrl = "$baseUrl/api/v1"
    private val json = Json { ignoreUnknownKeys = true }
    private var authToken: String? = null

    fun setAuthToken(token: String?) {
        authToken = token
    }

    private fun io.ktor.client.request.HttpRequestBuilder.applyAuth() {
        authToken?.let {
            headers.append(HttpHeaders.Authorization, "Bearer $it")
        }
    }

    private suspend inline fun <reified T> handle(response: HttpResponse): T {
        if (response.status.isSuccess()) {
            return response.body()
        }

        val statusCode = response.status.value

        // 5xx는 내부 서버 오류이므로, 구체적인 메시지 대신 공통 문구만 노출
        if (statusCode >= 500) {
            throw IllegalArgumentException("서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.")
        }

        val raw = try {
            response.bodyAsText()
        } catch (_: Exception) {
            null
        }

        val message = if (!raw.isNullOrBlank()) {
            try {
                json.decodeFromString(ErrorResponseDto.serializer(), raw).message
            } catch (_: Exception) {
                "요청을 처리할 수 없습니다. (${response.status.value})"
            }
        } else {
            "요청을 처리할 수 없습니다. (${response.status.value})"
        }

        throw IllegalArgumentException(message)
    }

    // 번호판 인식 이벤트 처리
    suspend fun postPlateDetected(request: PlateDetectedRequest): PlateDetectedResponse {
        val response = client.post("$apiBaseUrl/events/plate-detected") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return handle(response)
    }

    // 주차장 등록
    suspend fun createParkingLot(request: CreateParkingLotRequest): ParkingLotResponse {
        val response = client.post("$apiBaseUrl/parking-lots") {
            applyAuth()
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return handle(response)
    }

    // 주차장 목록 조회
    suspend fun getParkingLots(): List<ParkingLotResponse> {
        val response = client.get("$apiBaseUrl/parking-lots")
        return handle(response)
    }

    suspend fun getMyParkingLots(): List<ParkingLotResponse> {
        val response = client.get("$apiBaseUrl/parking-lots/my-lots") {
            applyAuth()
        }
        return handle(response)
    }

    suspend fun searchParkingLots(query: String): List<ParkingLotResponse> {
        val response = client.get("$apiBaseUrl/parking-lots/search") {
            parameter("q", query)
        }
        return handle(response)
    }

    // 주차장 조회
    suspend fun getParkingLot(id: Long): ParkingLotResponse {
        val response = client.get("$apiBaseUrl/parking-lots/$id")
        return handle(response)
    }

    // 게이트 등록
    suspend fun registerGate(request: RegisterGateRequest): GateResponse {
        val response = client.post("$apiBaseUrl/gates/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return handle(response)
    }

    // 게이트 목록 조회
    suspend fun getGates(parkingLotId: Long): List<GateResponse> {
        val response = client.get("$apiBaseUrl/gates") {
            parameter("parkingLotId", parkingLotId)
        }
        return handle(response)
    }

    // 차량 등록
    suspend fun createVehicle(request: CreateVehicleRequest): VehicleResponse {
        val response = client.post("$apiBaseUrl/vehicles") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return handle(response)
    }

    // 차량 목록 조회
    suspend fun getVehicles(parkingLotId: Long): List<VehicleResponse> {
        val response = client.get("$apiBaseUrl/vehicles") {
            parameter("parkingLotId", parkingLotId)
        }
        return handle(response)
    }

    // 현재 주차 중 목록
    suspend fun getOpenSessions(parkingLotId: Long): List<SessionResponse> {
        val response = client.get("$apiBaseUrl/sessions/open") {
            parameter("parkingLotId", parkingLotId)
        }
        return handle(response)
    }

    // 날짜별 입출차 기록
    suspend fun getSessionHistory(parkingLotId: Long, date: String): List<SessionResponse> {
        val response = client.get("$apiBaseUrl/sessions/history") {
            parameter("parkingLotId", parkingLotId)
            parameter("date", date)
        }
        return handle(response)
    }

    // 번호판으로 차량 조회
    suspend fun getVehicleByPlateNumber(parkingLotId: Long, plateNumber: String): VehicleResponse? {
        return try {
            val url = "$apiBaseUrl/parking-lots/$parkingLotId/vehicles/plate/$plateNumber"

            val response = client.get(url) {
                applyAuth()
            }
            kotlin.io.println("[API] 차량 조회 응답: status=${response.status.value}")

            if (response.status.value == 404) {
                kotlin.io.println("[API] 차량을 찾을 수 없음 (404)")
                return null
            }
            
            if (!response.status.isSuccess()) {
                kotlin.io.println("[API] 차량 조회 실패: status=${response.status.value}")
                val errorText = try {
                    response.bodyAsText()
                } catch (e: Exception) {
                    "응답 본문 읽기 실패: ${e.message}"
                }
                kotlin.io.println("[API] 에러 응답 본문: $errorText")
                return null
            }
            
            val vehicle = response.body<VehicleResponse?>()
            kotlin.io.println("[API] 차량 조회 성공: plateNumber=${vehicle?.plateNumber}, label=${vehicle?.label}")
            vehicle
        } catch (e: Exception) {
            kotlin.io.println("[API] 차량 조회 예외 발생: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    // 번호판으로 현재 세션 조회
    suspend fun getCurrentSessionByPlateNumber(parkingLotId: Long, plateNumber: String): SessionResponse? {
        return try {
            val url = "$apiBaseUrl/parking-lots/$parkingLotId/sessions/current/$plateNumber"
            
            val response = client.get(url) {
                applyAuth()
            }
            kotlin.io.println("[API] 세션 조회 응답: status=${response.status.value}")
            
            if (response.status.value == 404) {
                kotlin.io.println("[API] 세션을 찾을 수 없음 (404)")
                return null
            }
            
            if (!response.status.isSuccess()) {
                kotlin.io.println("[API] 세션 조회 실패: status=${response.status.value}")
                return null
            }
            
            val session = response.body<SessionResponse?>()
            kotlin.io.println("[API] 세션 조회 성공: plateNumber=${session?.plateNumber}")
            session
        } catch (e: Exception) {
            kotlin.io.println("[API] 세션 조회 예외 발생: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    override fun close() {
        client.close()
    }

    // Auth APIs
    suspend fun register(request: RegisterRequest): AuthResponse {
        val response = client.post("$apiBaseUrl/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return handle(response)
    }

    suspend fun login(request: LoginRequest): AuthResponse {
        val response = client.post("$apiBaseUrl/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return handle(response)
    }

    suspend fun getProfile(): UserResponse {
        val response = client.get("$apiBaseUrl/auth/me") {
            applyAuth()
        }
        return handle(response)
    }

    suspend fun loginWithKakao(request: KakaoLoginRequest): AuthResponse {
        val response = client.post("$apiBaseUrl/auth/oauth/kakao") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return handle(response)
    }

    // Membership APIs
    suspend fun getParkingLotMembers(parkingLotId: Long): List<ParkingLotMemberResponse> {
        val response = client.get("$apiBaseUrl/parking-lots/$parkingLotId/members") {
            applyAuth()
        }
        return handle(response)
    }

    suspend fun requestJoinParkingLot(parkingLotId: Long): ParkingLotMemberResponse {
        val response = client.post("$apiBaseUrl/parking-lots/$parkingLotId/members/join-request") {
            applyAuth()
        }
        return handle(response)
    }

    suspend fun inviteParkingLotMember(
        parkingLotId: Long,
        request: InviteMemberRequest
    ): ParkingLotMemberResponse {
        val response = client.post("$apiBaseUrl/parking-lots/$parkingLotId/members/invite") {
            applyAuth()
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return handle(response)
    }

    suspend fun approveParkingLotMember(parkingLotId: Long, userId: String): ParkingLotMemberResponse {
        val response = client.put("$apiBaseUrl/parking-lots/$parkingLotId/members/$userId/approve") {
            applyAuth()
        }
        return handle(response)
    }

    suspend fun rejectParkingLotMember(parkingLotId: Long, userId: String): ParkingLotMemberResponse {
        val response = client.put("$apiBaseUrl/parking-lots/$parkingLotId/members/$userId/reject") {
            applyAuth()
        }
        return handle(response)
    }

    suspend fun changeMemberRole(
        parkingLotId: Long,
        userId: String,
        request: UpdateMemberRoleRequest
    ): ParkingLotMemberResponse {
        val response = client.put("$apiBaseUrl/parking-lots/$parkingLotId/members/$userId/role") {
            applyAuth()
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return handle(response)
    }

    suspend fun removeParkingLotMember(parkingLotId: Long, userId: String) {
        val response = client.delete("$apiBaseUrl/parking-lots/$parkingLotId/members/$userId") {
            applyAuth()
        }
        handle<Unit>(response)
    }
}
