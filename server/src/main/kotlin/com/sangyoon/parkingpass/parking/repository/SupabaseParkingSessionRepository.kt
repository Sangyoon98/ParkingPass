package com.sangyoon.parkingpass.parking.repository

import com.sangyoon.parkingpass.common.KOREA_ZONE_ID
import com.sangyoon.parkingpass.parking.model.ParkingSession
import com.sangyoon.parkingpass.parking.model.SessionStatus
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import java.time.LocalDate

class SupabaseParkingSessionRepository(
    private val supabase: SupabaseClient
) : ParkingSessionRepository {

    override suspend fun findOpenSessionByParkingLotIdAndPlateNumber(
        parkingLotId: Long,
        plateNumber: String
    ): ParkingSession? {
        return try {
            supabase.from("parking_session")
                .select {
                    filter {
                        eq("parking_lot_id", parkingLotId)
                        eq("plate_number", plateNumber)
                        eq("status", SessionStatus.OPEN.name)
                    }
                }
                .decodeSingle<ParkingSession>()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun save(session: ParkingSession): ParkingSession {
        return try {
            if (session.id == 0L) {
                // Insert: 저장만 하고, 세션 정보는 그대로 반환 (ID는 이후 조회 시 Supabase에서 사용)
                supabase.from("parking_session")
                    .insert(session)
                session
            } else {
                // Update: 업데이트만 수행하고, 수정된 세션 객체를 그대로 반환
                supabase.from("parking_session")
                    .update(session) {
                        filter { eq("id", session.id) }
                    }
                session
            }
        } catch (e: Exception) {
            // Supabase 오류 발생 시 그대로 예외를 올려보냄 (StatusPages에서 처리)
            throw e
        }
    }

    override suspend fun findAllOpenSessions(parkingLotId: Long): List<ParkingSession> {
        return supabase.from("parking_session")
            .select {
                filter {
                    eq("parking_lot_id", parkingLotId)
                    eq("status", SessionStatus.OPEN.name)
                }
            }
            .decodeList<ParkingSession>()
    }

    override suspend fun findAllByParkingLotIdAndDate(
        parkingLotId: Long,
        date: LocalDate
    ): List<ParkingSession> {
        val startOfDay = date.atStartOfDay(KOREA_ZONE_ID).toInstant()
        val endOfDay = date.plusDays(1).atStartOfDay(KOREA_ZONE_ID).toInstant()

        return supabase.from("parking_session")
            .select {
                filter {
                    eq("parking_lot_id", parkingLotId)
                    gte("entered_at", startOfDay.toString())
                    lt("entered_at", endOfDay.toString())
                }
            }
            .decodeList<ParkingSession>()
    }
}

