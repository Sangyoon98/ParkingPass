package com.sangyoon.parkingpass.domain.usecase

import com.sangyoon.parkingpass.domain.repository.ParkingLotMemberRepository

class RemoveParkingLotMemberUseCase(
    private val repository: ParkingLotMemberRepository
) {
    suspend operator fun invoke(
        parkingLotId: Long,
        userId: String
    ): Result<Unit> = repository.removeMember(parkingLotId, userId)
}
