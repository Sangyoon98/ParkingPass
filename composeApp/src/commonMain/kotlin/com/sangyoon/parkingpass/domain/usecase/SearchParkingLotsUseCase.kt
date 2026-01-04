package com.sangyoon.parkingpass.domain.usecase

import com.sangyoon.parkingpass.domain.model.ParkingLot
import com.sangyoon.parkingpass.domain.repository.ParkingLotRepository

class SearchParkingLotsUseCase(
    private val repository: ParkingLotRepository
) {
    suspend operator fun invoke(query: String): Result<List<ParkingLot>> =
        repository.searchParkingLots(query)
}
