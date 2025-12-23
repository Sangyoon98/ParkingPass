package com.sangyoon.parkingpass.parking.repository

import com.sangyoon.parkingpass.parking.model.ParkingLot
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns

class SupabaseParkingLotRepository(
    private val supabase: SupabaseClient
) : ParkingLotRepository {

    override suspend fun save(lot: ParkingLot): ParkingLot {
        return if (lot.id == 0L) {
            // Insert: 생성된 ID를 포함한 레코드를 반환
            supabase.from("parking_lot")
                .insert(lot) {
                    select()
                }
                .decodeSingle<ParkingLot>()
        } else {
            // Update: 업데이트된 레코드를 반환
            supabase.from("parking_lot")
                .update(lot) {
                    filter { eq("id", lot.id) }
                    select()
                }
                .decodeSingle<ParkingLot>()
        }
    }

    override suspend fun findById(id: Long): ParkingLot? {
        return try {
            supabase.from("parking_lot")
                .select {
                    filter {
                        eq("id", id)
                    }
                }
                .decodeSingle<ParkingLot>()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun findAll(): List<ParkingLot> {
        return supabase.from("parking_lot")
            .select(columns = Columns.ALL)
            .decodeList<ParkingLot>()
    }
}

