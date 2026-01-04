package com.sangyoon.parkingpass.domain.usecase

import com.sangyoon.parkingpass.domain.model.MemberRole
import com.sangyoon.parkingpass.domain.model.ParkingLotMember
import com.sangyoon.parkingpass.domain.repository.ParkingLotMemberRepository

class UpdateMemberRoleUseCase(
    private val repository: ParkingLotMemberRepository
) {
    suspend operator fun invoke(
        parkingLotId: Long,
        userId: String,
        role: MemberRole
    ): Result<ParkingLotMember> = repository.updateMemberRole(parkingLotId, userId, role)
}
