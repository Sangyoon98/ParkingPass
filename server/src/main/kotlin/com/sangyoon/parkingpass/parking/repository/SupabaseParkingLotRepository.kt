package com.sangyoon.parkingpass.parking.repository

import com.sangyoon.parkingpass.parking.model.ParkingLot
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns

class SupabaseParkingLotRepository(
    private val supabase: SupabaseClient
) : ParkingLotRepository {

    override suspend fun save(lot: ParkingLot): ParkingLot {
        return try {
            if (lot.id == 0L) {
                // Insert: Supabase에만 저장하고, 우리가 가진 객체를 그대로 반환
                supabase.from("parking_lot")
                    .insert(lot)
                lot
            } else {
                // Update: Supabase에만 반영하고, 수정된 객체를 그대로 반환
                supabase.from("parking_lot")
                    .update(lot) {
                        filter { eq("id", lot.id) }
                    }
                lot
            }
        } catch (e: Exception) {
            // 저장 중 예외는 그대로 상위로 올려 보냄 (StatusPages에서 처리)
            throw e
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

