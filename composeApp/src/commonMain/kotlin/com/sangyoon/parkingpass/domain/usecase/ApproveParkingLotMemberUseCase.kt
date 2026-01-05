package com.sangyoon.parkingpass.domain.usecase

import com.sangyoon.parkingpass.domain.model.ParkingLotMember
import com.sangyoon.parkingpass.domain.repository.ParkingLotMemberRepository

class ApproveParkingLotMemberUseCase(
    private val repository: ParkingLotMemberRepository
) {
    suspend operator fun invoke(
        parkingLotId: Long,
        userId: String
    ): Result<ParkingLotMember> = repository.approveMember(parkingLotId, userId)
}
