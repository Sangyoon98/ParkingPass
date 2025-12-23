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
        return try {
            supabase.from("vehicle")
                .select {
                    filter {
                        eq("parking_lot_id", parkingLotId)
                        eq("plate_number", plateNumber)
                    }
                }
                .decodeSingle<Vehicle>()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun save(vehicle: Vehicle): Vehicle {
        return try {
            if (vehicle.id == 0L) {
                // Insert: Supabase에만 저장하고, 우리가 가진 객체를 그대로 반환
                supabase.from("vehicle")
                    .insert(vehicle)
                vehicle
            } else {
                // Update: Supabase에만 반영하고, 수정된 객체를 그대로 반환
                supabase.from("vehicle")
                    .update(vehicle) {
                        filter { eq("id", vehicle.id) }
                    }
                vehicle
            }
        } catch (e: Exception) {
            // 저장 중 예외는 그대로 상위로 올려 보냄 (StatusPages에서 처리)
            throw e
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
            // 조회 실패 시 빈 목록 반환 (UI 에러 대신 "차량 없음" 상태로 처리)
            emptyList()
        }
    }
}

