package com.sangyoon.parkingpass.domain.repository

import com.sangyoon.parkingpass.domain.model.MemberRole
import com.sangyoon.parkingpass.domain.model.ParkingLotMember

interface ParkingLotMemberRepository {
    suspend fun getMembers(parkingLotId: Long): Result<List<ParkingLotMember>>
    suspend fun requestJoin(parkingLotId: Long): Result<ParkingLotMember>
    suspend fun inviteMember(parkingLotId: Long, email: String, role: MemberRole): Result<ParkingLotMember>
    suspend fun approveMember(parkingLotId: Long, userId: String): Result<ParkingLotMember>
    suspend fun rejectMember(parkingLotId: Long, userId: String): Result<ParkingLotMember>
    suspend fun updateMemberRole(parkingLotId: Long, userId: String, role: MemberRole): Result<ParkingLotMember>
    suspend fun removeMember(parkingLotId: Long, userId: String): Result<Unit>
}
