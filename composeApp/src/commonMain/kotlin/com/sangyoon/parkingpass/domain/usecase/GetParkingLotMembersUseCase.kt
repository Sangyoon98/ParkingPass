package com.sangyoon.parkingpass.domain.usecase

import com.sangyoon.parkingpass.domain.model.ParkingLotMember
import com.sangyoon.parkingpass.domain.repository.ParkingLotMemberRepository

class GetParkingLotMembersUseCase(
    private val repository: ParkingLotMemberRepository
) {
    suspend operator fun invoke(parkingLotId: Long): Result<List<ParkingLotMember>> =
        repository.getMembers(parkingLotId)
}
