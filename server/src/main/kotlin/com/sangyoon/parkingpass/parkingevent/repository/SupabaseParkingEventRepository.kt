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
            // Insert: 생성된 ID를 포함한 레코드 반환
            supabase.from("parking_event")
                .insert(event) {
                    select()
                }
                .decodeSingle<ParkingEvent>()
        } else {
            // Update: 업데이트된 레코드 반환
            supabase.from("parking_event")
                .update(event) {
                    filter { eq("id", event.id) }
                    select()
                }
                .decodeSingle<ParkingEvent>()
        }
    }

    override suspend fun findAll(): List<ParkingEvent> {
        return supabase.from("parking_event")
            .select(columns = Columns.ALL)
            .decodeList<ParkingEvent>()
    }
}

