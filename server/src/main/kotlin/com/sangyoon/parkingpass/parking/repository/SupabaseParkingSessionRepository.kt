package com.sangyoon.parkingpass.parking.repository

import com.sangyoon.parkingpass.parking.model.ParkingSession
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import java.time.LocalDate

class SupabaseParkingSessionRepository(
    private val supabase: SupabaseClient
) : ParkingSessionRepository {

    override suspend fun findOpenSessionByParkingLotIdAndPlateNumber(
        parkingLotId: Long,
        plateNumber: String
    ): ParkingSession? {
        return supabase.from("parking_session")
            .select {
                filter {
                    eq("parking_lot_id", parkingLotId)
                    eq("plate_number", plateNumber)
                    eq("status", "OPEN")
                }
            }
            .decodeSingleOrNull<ParkingSession>()
    }

    override suspend fun save(session: ParkingSession): ParkingSession {
        return if (session.id == 0L) {
            // Insert: Supabase에 저장하고 생성된 객체 반환
            supabase.from("parking_session")
                .insert(session) {
                    select(Columns.ALL)
                }
                .decodeSingle<ParkingSession>()
        } else {
            // Update: 업데이트하고 생성된 객체 반환
            val updated = supabase.from("parking_session")
                .update(session) {
                    filter {
                        eq("id", session.id)
                    }
                    select(Columns.ALL)
                }
                .decodeList<ParkingSession>()
                .singleOrNull()
                ?: throw IllegalStateException("Session not found: ${session.id}")
            updated
        }
    }

    override suspend fun findAllOpenSessions(parkingLotId: Long): List<ParkingSession> {
        return supabase.from("parking_session")
            .select {
                filter {
                    eq("parking_lot_id", parkingLotId)
                    eq("status", "OPEN")
                }
            }
            .decodeList<ParkingSession>()
    }

    override suspend fun findAllByParkingLotIdAndDate(
        parkingLotId: Long,
        date: LocalDate
    ): List<ParkingSession> {
        val startOfDay = date.atStartOfDay().toInstant(java.time.ZoneOffset.UTC)
        val endOfDay = date.plusDays(1).atStartOfDay().toInstant(java.time.ZoneOffset.UTC)

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

