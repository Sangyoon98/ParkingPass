package com.sangyoon.parkingpass.parking.repository

import com.sangyoon.parkingpass.parking.model.Vehicle
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

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
            // Insert: Supabase에 저장 (JSON 파싱 이슈 방지를 위해 입력 객체 반환)
            supabase.from("vehicle")
                .insert(vehicle)
            vehicle
        } else {
            // Update: 업데이트 (JSON 파싱 이슈 방지를 위해 입력 객체 반환)
            supabase.from("vehicle")
                .update(vehicle) {
                    filter {
                        eq("id", vehicle.id)
                    }
                }
            vehicle
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

