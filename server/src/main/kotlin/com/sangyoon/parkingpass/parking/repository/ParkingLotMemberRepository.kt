package com.sangyoon.parkingpass.parking.repository

import com.sangyoon.parkingpass.parking.model.MemberRole
import com.sangyoon.parkingpass.parking.model.MemberStatus
import com.sangyoon.parkingpass.parking.model.ParkingLotMember
import java.util.UUID

interface ParkingLotMemberRepository {
    suspend fun findByParkingLotId(parkingLotId: Long): List<ParkingLotMember>
    suspend fun findByUserId(userId: UUID): List<ParkingLotMember>
    suspend fun findByParkingLotIdAndUserId(parkingLotId: Long, userId: UUID): ParkingLotMember?
    suspend fun save(member: ParkingLotMember): ParkingLotMember
    suspend fun updateStatus(memberId: Long, status: MemberStatus): ParkingLotMember
    suspend fun updateRole(memberId: Long, role: MemberRole): ParkingLotMember
    suspend fun delete(memberId: Long)
}
