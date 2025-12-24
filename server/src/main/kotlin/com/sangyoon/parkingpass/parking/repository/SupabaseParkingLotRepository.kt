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
            // Insert: Supabase에 저장하고 생성된 객체 반환
            supabase.from("parking_lot")
                .insert(lot) {
                    select(Columns.ALL)
                }
                .decodeSingle<ParkingLot>()
        } else {
            // Update: 업데이트하고 생성된 객체 반환
            supabase.from("parking_lot")
                .update(lot) {
                    filter {
                        eq("id", lot.id)
                    }
                    select(Columns.ALL)
                }
                .decodeSingle<ParkingLot>()
        }
    }

    override suspend fun findById(id: Long): ParkingLot? {
        return supabase.from("parking_lot")
            .select {
                filter {
                    eq("id", id)
                }
            }
            .decodeSingleOrNull<ParkingLot>()
    }

    override suspend fun findAll(): List<ParkingLot> {
        return supabase.from("parking_lot")
            .select()
            .decodeList<ParkingLot>()
    }
}

