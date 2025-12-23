package com.sangyoon.parkingpass.parking.repository

import com.sangyoon.parkingpass.parking.model.GateDevice
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns

class SupabaseGateDeviceRepository(
    private val supabase: SupabaseClient
) : GateDeviceRepository {

    override suspend fun findByDeviceKey(deviceKey: String): GateDevice? {
        return try {
            supabase.from("gate_device")
                .select {
                    filter {
                        eq("device_key", deviceKey)
                    }
                }
                .decodeSingle<GateDevice>()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun save(device: GateDevice): GateDevice {
        return try {
            if (device.id == 0L) {
                // Insert: Supabase에만 저장하고, 우리가 가진 객체를 그대로 반환
                supabase.from("gate_device")
                    .insert(device)
                device
            } else {
                // Update: Supabase에만 반영하고, 수정된 객체를 그대로 반환
                supabase.from("gate_device")
                    .update(device) {
                        filter { eq("id", device.id) }
                    }
                device
            }
        } catch (e: Exception) {
            // 저장 중 예외는 그대로 상위로 올려 보냄
            throw e
        }
    }

    override suspend fun findAllByParkingLotId(parkingLotId: Long): List<GateDevice> {
        return supabase.from("gate_device")
            .select {
                filter {
                    eq("parking_lot_id", parkingLotId)
                }
            }
            .decodeList<GateDevice>()
    }
}

