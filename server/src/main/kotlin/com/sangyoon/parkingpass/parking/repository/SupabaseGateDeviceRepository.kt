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
        return if (device.id == 0L) {
            // Insert: 생성된 ID를 포함한 전체 레코드를 반환받음
            supabase.from("gate_device")
                .insert(device) {
                    select()
                }
                .decodeSingle<GateDevice>()
        } else {
            // Update: 업데이트된 레코드를 반환받음
            supabase.from("gate_device")
                .update(device) {
                    filter {
                        eq("id", device.id)
                    }
                    select()
                }
                .decodeSingle<GateDevice>()
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

