package com.sangyoon.parkingpass.data.datasource

import com.sangyoon.parkingpass.api.ParkingApiClient
import com.sangyoon.parkingpass.api.dto.AuthResponse
import com.sangyoon.parkingpass.api.dto.CreateParkingLotRequest
import com.sangyoon.parkingpass.api.dto.CreateVehicleRequest
import com.sangyoon.parkingpass.api.dto.InviteMemberRequest
import com.sangyoon.parkingpass.api.dto.KakaoLoginRequest
import com.sangyoon.parkingpass.api.dto.LoginRequest
import com.sangyoon.parkingpass.api.dto.ParkingLotMemberResponse
import com.sangyoon.parkingpass.api.dto.ParkingLotResponse
import com.sangyoon.parkingpass.api.dto.PlateDetectedRequest
import com.sangyoon.parkingpass.api.dto.RegisterGateRequest
import com.sangyoon.parkingpass.api.dto.RegisterRequest
import com.sangyoon.parkingpass.api.dto.UpdateMemberRoleRequest
import com.sangyoon.parkingpass.api.dto.UserResponse

class ParkingApiDataSource(
    private val apiClient: ParkingApiClient
) {
    fun setAuthToken(token: String?) {
        apiClient.setAuthToken(token)
    }

    suspend fun getParkingLots(): List<ParkingLotResponse> = apiClient.getParkingLots()

    suspend fun getParkingLot(id: Long): ParkingLotResponse = apiClient.getParkingLot(id)

    suspend fun createParkingLot(request: CreateParkingLotRequest): ParkingLotResponse = apiClient.createParkingLot(request)

    suspend fun getOpenSessions(parkingLotId: Long) = apiClient.getOpenSessions(parkingLotId)

    suspend fun getSessionHistory(parkingLotId: Long, date: String) = apiClient.getSessionHistory(parkingLotId, date)

    suspend fun createVehicle(request: CreateVehicleRequest) = apiClient.createVehicle(request)

    suspend fun getVehicles(parkingLotId: Long) = apiClient.getVehicles(parkingLotId)

    suspend fun getGates(parkingLotId: Long) = apiClient.getGates(parkingLotId)

    suspend fun registerGate(request: RegisterGateRequest) = apiClient.registerGate(request)

    suspend fun detectPlate(request: PlateDetectedRequest) = apiClient.postPlateDetected(request)

    suspend fun getVehicleByPlateNumber(parkingLotId: Long, plateNumber: String) = apiClient.getVehicleByPlateNumber(parkingLotId, plateNumber)

    suspend fun getCurrentSessionByPlateNumber(parkingLotId: Long, plateNumber: String) = apiClient.getCurrentSessionByPlateNumber(parkingLotId, plateNumber)

    suspend fun register(registerRequest: RegisterRequest): AuthResponse = apiClient.register(registerRequest)

    suspend fun login(loginRequest: LoginRequest): AuthResponse = apiClient.login(loginRequest)

    suspend fun getProfile(): UserResponse = apiClient.getProfile()

    suspend fun loginWithKakao(
        code: String? = null,
        redirectUri: String? = null,
        accessToken: String? = null
    ): AuthResponse =
        apiClient.loginWithKakao(KakaoLoginRequest(code = code, redirectUri = redirectUri, accessToken = accessToken))

    suspend fun getMyParkingLots(): List<ParkingLotResponse> = apiClient.getMyParkingLots()

    suspend fun searchParkingLots(query: String): List<ParkingLotResponse> = apiClient.searchParkingLots(query)

    suspend fun getParkingLotMembers(parkingLotId: Long): List<ParkingLotMemberResponse> =
        apiClient.getParkingLotMembers(parkingLotId)

    suspend fun requestJoinParkingLot(parkingLotId: Long): ParkingLotMemberResponse =
        apiClient.requestJoinParkingLot(parkingLotId)

    suspend fun inviteParkingLotMember(parkingLotId: Long, request: InviteMemberRequest): ParkingLotMemberResponse =
        apiClient.inviteParkingLotMember(parkingLotId, request)

    suspend fun approveParkingLotMember(parkingLotId: Long, userId: String): ParkingLotMemberResponse =
        apiClient.approveParkingLotMember(parkingLotId, userId)

    suspend fun rejectParkingLotMember(parkingLotId: Long, userId: String): ParkingLotMemberResponse =
        apiClient.rejectParkingLotMember(parkingLotId, userId)

    suspend fun changeMemberRole(
        parkingLotId: Long,
        userId: String,
        request: UpdateMemberRoleRequest
    ): ParkingLotMemberResponse = apiClient.changeMemberRole(parkingLotId, userId, request)

    suspend fun removeParkingLotMember(parkingLotId: Long, userId: String) =
        apiClient.removeParkingLotMember(parkingLotId, userId)
}
