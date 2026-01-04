package com.sangyoon.parkingpass.domain.usecase

import com.sangyoon.parkingpass.domain.model.ParkingLotMember
import com.sangyoon.parkingpass.domain.repository.ParkingLotMemberRepository

class RequestJoinParkingLotUseCase(
    private val repository: ParkingLotMemberRepository
) {
    suspend operator fun invoke(parkingLotId: Long): Result<ParkingLotMember> =
        repository.requestJoin(parkingLotId)
}
