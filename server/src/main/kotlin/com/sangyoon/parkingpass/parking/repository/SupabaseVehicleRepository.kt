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
        println("🔍 [SupabaseVehicleRepository] DB 조회 - parkingLotId: $parkingLotId, plateNumber: '$plateNumber'")
        println("🔍 [SupabaseVehicleRepository] plateNumber 길이: ${plateNumber.length}, 바이트: ${plateNumber.toByteArray().contentToString()}")
        
        val result = try {
            supabase.from("vehicle")
                .select {
                    filter {
                        eq("parking_lot_id", parkingLotId)
                        eq("plate_number", plateNumber)
                    }
                }
                .decodeSingleOrNull<Vehicle>()
        } catch (e: Exception) {
            println("💥 [SupabaseVehicleRepository] DB 조회 예외: ${e.message}")
            e.printStackTrace()
            null
        }
        
        if (result != null) {
            println("✅ [SupabaseVehicleRepository] 차량 찾음: id=${result.id}, plateNumber='${result.plateNumber}'")
            println("🔍 [SupabaseVehicleRepository] DB plateNumber 길이: ${result.plateNumber.length}, 바이트: ${result.plateNumber.toByteArray().contentToString()}")
        } else {
            println("❌ [SupabaseVehicleRepository] 차량을 찾을 수 없음")
            // 모든 차량 조회해서 비교
            try {
                val allVehicles = supabase.from("vehicle")
                    .select {
                        filter {
                            eq("parking_lot_id", parkingLotId)
                        }
                    }
                    .decodeList<Vehicle>()
                println("🔍 [SupabaseVehicleRepository] 해당 주차장의 모든 차량: ${allVehicles.map { "'${it.plateNumber}'" }}")
            } catch (e: Exception) {
                println("💥 [SupabaseVehicleRepository] 전체 조회 실패: ${e.message}")
            }
        }
        
        return result
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

