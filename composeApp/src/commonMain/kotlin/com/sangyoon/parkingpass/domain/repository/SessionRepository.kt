package com.sangyoon.parkingpass.domain.repository

import com.sangyoon.parkingpass.domain.model.Session

interface SessionRepository {
    suspend fun getOpenSessions(parkingLotId: Long): Result<List<Session>>
    suspend fun getSessionHistory(parkingLotId: Long, date: String): Result<List<Session>>
}