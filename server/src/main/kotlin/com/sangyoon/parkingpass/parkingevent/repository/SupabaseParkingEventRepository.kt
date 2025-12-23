package com.sangyoon.parkingpass.parkingevent.repository

import com.sangyoon.parkingpass.parkingevent.model.ParkingEvent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns

class SupabaseParkingEventRepository(
    private val supabase: SupabaseClient
) : ParkingEventRepository {

    override suspend fun save(event: ParkingEvent): ParkingEvent {
        return try {
            if (event.id == 0L) {
                // Insert: 저장만 수행
                supabase.from("parking_event")
                    .insert(event)
                event
            } else {
                // Update: 업데이트만 수행
                supabase.from("parking_event")
                    .update(event) {
                        filter { eq("id", event.id) }
                    }
                event
            }
        } catch (e: Exception) {
            // Supabase 오류는 상위에서 처리
            throw e
        }
    }

    override suspend fun findAll(): List<ParkingEvent> {
        return supabase.from("parking_event")
            .select(columns = Columns.ALL)
            .decodeList<ParkingEvent>()
    }
}

