package com.sangyoon.parkingpass.parking.repository

import com.sangyoon.parkingpass.parking.model.Vehicle
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns

class SupabaseVehicleRepository(
    private val supabase: SupabaseClient
) : VehicleRepository {

    override suspend fun findByParkingLotIdAndPlateNumber(
        parkingLotId: Long,
        plateNumber: String
    ): Vehicle? {
        return supabase.from("vehicle")
            .select {
                filter {
                    eq("parking_lot_id", parkingLotId)
                    eq("plate_number", plateNumber)
                }
            }
            .decodeSingleOrNull<Vehicle>()
    }

    override suspend fun save(vehicle: Vehicle): Vehicle {
        return if (vehicle.id == 0L) {
            // Insert: 저장하고 생성된 객체 반환
            supabase.from("vehicle")
                .insert(vehicle) {
                    select(Columns.ALL)
                }
                .decodeSingle<Vehicle>()
        } else {
            // Update: 업데이트하고 생성된 객체 반환
            supabase.from("vehicle")
                .update(vehicle) {
                    filter {
                        eq("id", vehicle.id)
                    }
                    select(Columns.ALL)
                }
                .decodeSingle<Vehicle>()
        }
    }

    override suspend fun findAllByParkingLotId(parkingLotId: Long): List<Vehicle> {
        return try {
            supabase.from("vehicle")
                .select {
                    filter {
                        eq("parking_lot_id", parkingLotId)
                    }
                }
                .decodeList<Vehicle>()
        } catch (e: Exception) {
            // 빈 결과나 에러 시 빈 리스트 반환
            emptyList()
        }
    }
}

