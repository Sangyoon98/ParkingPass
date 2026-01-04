package com.sangyoon.parkingpass.parking.repository

import com.sangyoon.parkingpass.parking.model.ParkingLot
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

class ImMemoryParkingLotRepository : ParkingLotRepository {
    private val idGenerator = AtomicLong(1L)
    private val lots = ConcurrentHashMap<Long, ParkingLot>()

    override suspend fun save(lot: ParkingLot): ParkingLot {
        val id = if (lot.id != 0L) lot.id else idGenerator.getAndIncrement()
        val saved = lot.copy(id = id)
        lots[id] = saved
        return saved
    }

    override suspend fun findById(id: Long): ParkingLot? = lots[id]

    override suspend fun findAll(): List<ParkingLot> = lots.values.toList()

    override suspend fun findByIds(ids: Collection<Long>): List<ParkingLot> =
        ids.mapNotNull { lots[it] }

    override suspend fun searchPublicLots(query: String, limit: Int): List<ParkingLot> {
        val normalized = query.trim()
        val source = lots.values.filter { it.isPublic }
        if (normalized.isEmpty()) {
            return source.take(limit)
        }
        return source.filter {
            it.name.contains(normalized, ignoreCase = true) ||
                it.location.contains(normalized, ignoreCase = true)
        }.take(limit)
    }
}
