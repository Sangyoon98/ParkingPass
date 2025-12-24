package com.sangyoon.parkingpass.parkingevent.repository

import com.sangyoon.parkingpass.parkingevent.model.ParkingEvent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns

class SupabaseParkingEventRepository(
    private val supabase: SupabaseClient
) : ParkingEventRepository {

    override suspend fun save(event: ParkingEvent): ParkingEvent {
        return if (event.id == 0L) {
            // Insert: 저장 후 생성된 객체 반환
            supabase.from("parking_event")
                .insert(event) {
                    select(Columns.ALL)
                }
                .decodeSingle<ParkingEvent>()
        } else {
            // Update: 업데이트 후 생성된 객체 반환
            val updated = supabase.from("parking_event")
                .update(event) {
                    filter {
                        eq("id", event.id)
                    }
                    select(Columns.ALL)
                }
                .decodeList<ParkingEvent>()
                .singleOrNull()
                ?: throw IllegalStateException("Event not found: ${event.id}")
            updated
        }
    }

    override suspend fun findAll(): List<ParkingEvent> {
        return supabase.from("parking_event")
            .select()
            .decodeList<ParkingEvent>()
    }
}

