package com.sangyoon.parkingpass.parkinglot.service

import com.sangyoon.parkingpass.auth.repository.UserRepository
import com.sangyoon.parkingpass.parking.model.MemberRole
import com.sangyoon.parkingpass.parking.model.MemberStatus
import com.sangyoon.parkingpass.parking.model.ParkingLot
import com.sangyoon.parkingpass.parking.model.ParkingLotMember
import com.sangyoon.parkingpass.parking.repository.ParkingLotMemberRepository
import com.sangyoon.parkingpass.parking.repository.ParkingLotRepository
import com.sangyoon.parkingpass.parkinglot.dto.CreateParkingLotRequest
import com.sangyoon.parkingpass.parkinglot.dto.ParkingLotResponse
import java.util.UUID
import kotlin.random.Random

class ParkingLotService(
    private val parkingLotRepository: ParkingLotRepository,
    private val parkingLotMemberRepository: ParkingLotMemberRepository,
    private val userRepository: UserRepository
) {
    suspend fun createParkingLot(ownerId: UUID, request: CreateParkingLotRequest): ParkingLotResponse {
        val owner = userRepository.findById(ownerId)
            ?: throw IllegalArgumentException("존재하지 않는 사용자입니다.")

        val lot = ParkingLot(
            id = 0L,
            name = request.name,
            location = request.location,
            ownerId = owner.id,
            isPublic = request.isPublic,
            joinCode = if (request.isPublic) null else generateJoinCode()
        )

        val saved = parkingLotRepository.save(lot)
        parkingLotMemberRepository.save(
            ParkingLotMember(
                parkingLotId = saved.id,
                userId = owner.id,
                role = MemberRole.OWNER,
                status = MemberStatus.APPROVED
            )
        )

        return toResponse(saved)
    }

    suspend fun getAllParkingLots(): List<ParkingLotResponse> {
        return parkingLotRepository.findAll().map { toResponse(it) }
    }

    suspend fun getMyParkingLots(userId: UUID): List<ParkingLotResponse> {
        val memberships = parkingLotMemberRepository.findByUserId(userId)
            .filter { it.status == MemberStatus.APPROVED }
        if (memberships.isEmpty()) {
            return emptyList()
        }

        val lotMap = parkingLotRepository
            .findByIds(memberships.map { it.parkingLotId }.toSet())
            .associateBy { it.id }

        return memberships.mapNotNull { lotMap[it.parkingLotId] }
            .map { toResponse(it) }
    }

    suspend fun searchPublicLots(query: String): List<ParkingLotResponse> {
        return parkingLotRepository.searchPublicLots(query).map { toResponse(it) }
    }

    suspend fun getParkingLot(id: Long): ParkingLotResponse {
        val lot = parkingLotRepository.findById(id)
            ?: throw IllegalArgumentException("주차장을 찾을 수 없습니다: $id")
        return toResponse(lot)
    }

    private fun toResponse(lot: ParkingLot): ParkingLotResponse =
        ParkingLotResponse(
            id = lot.id,
            name = lot.name,
            location = lot.location,
            ownerId = lot.ownerId?.toString(),
            isPublic = lot.isPublic,
            joinCode = lot.joinCode
        )

    private fun generateJoinCode(): String {
        val chars = ('A'..'Z') + ('0'..'9')
        return (1..8).map { chars.random(Random) }.joinToString("")
    }
}
