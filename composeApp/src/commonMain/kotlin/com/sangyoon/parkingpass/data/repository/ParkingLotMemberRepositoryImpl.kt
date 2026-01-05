package com.sangyoon.parkingpass.data.repository

import com.sangyoon.parkingpass.api.dto.InviteMemberRequest
import com.sangyoon.parkingpass.api.dto.ParkingLotMemberResponse
import com.sangyoon.parkingpass.api.dto.UpdateMemberRoleRequest
import com.sangyoon.parkingpass.data.datasource.ParkingApiDataSource
import com.sangyoon.parkingpass.domain.model.MemberRole
import com.sangyoon.parkingpass.domain.model.MemberStatus
import com.sangyoon.parkingpass.domain.model.ParkingLotMember
import com.sangyoon.parkingpass.domain.repository.ParkingLotMemberRepository

class ParkingLotMemberRepositoryImpl(
    private val dataSource: ParkingApiDataSource
) : ParkingLotMemberRepository {

    override suspend fun getMembers(parkingLotId: Long): Result<List<ParkingLotMember>> = runCatching {
        dataSource.getParkingLotMembers(parkingLotId).map { it.toDomain() }
    }

    override suspend fun requestJoin(parkingLotId: Long): Result<ParkingLotMember> = runCatching {
        dataSource.requestJoinParkingLot(parkingLotId).toDomain()
    }

    override suspend fun inviteMember(
        parkingLotId: Long,
        email: String,
        role: MemberRole
    ): Result<ParkingLotMember> = runCatching {
        dataSource.inviteParkingLotMember(
            parkingLotId,
            InviteMemberRequest(email = email, role = role.name)
        ).toDomain()
    }

    override suspend fun approveMember(parkingLotId: Long, userId: String): Result<ParkingLotMember> = runCatching {
        dataSource.approveParkingLotMember(parkingLotId, userId).toDomain()
    }

    override suspend fun rejectMember(parkingLotId: Long, userId: String): Result<ParkingLotMember> = runCatching {
        dataSource.rejectParkingLotMember(parkingLotId, userId).toDomain()
    }

    override suspend fun updateMemberRole(
        parkingLotId: Long,
        userId: String,
        role: MemberRole
    ): Result<ParkingLotMember> = runCatching {
        dataSource.changeMemberRole(parkingLotId, userId, UpdateMemberRoleRequest(role.name)).toDomain()
    }

    override suspend fun removeMember(parkingLotId: Long, userId: String): Result<Unit> = runCatching {
        dataSource.removeParkingLotMember(parkingLotId, userId)
    }
}

private fun ParkingLotMemberResponse.toDomain(): ParkingLotMember {
    val roleEnum = runCatching { MemberRole.valueOf(role) }.getOrElse { MemberRole.MEMBER }
    val statusEnum = runCatching { MemberStatus.valueOf(status) }.getOrElse { MemberStatus.PENDING }
    return ParkingLotMember(
        id = id,
        userId = userId,
        email = email,
        name = name,
        role = roleEnum,
        status = statusEnum,
        invitedBy = invitedBy,
        joinedAt = joinedAt
    )
}
