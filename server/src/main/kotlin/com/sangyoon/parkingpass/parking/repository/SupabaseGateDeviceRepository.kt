package com.sangyoon.parkingpass.parking.repository

import com.sangyoon.parkingpass.parking.model.GateDevice
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns

class SupabaseGateDeviceRepository(
    private val supabase: SupabaseClient
) : GateDeviceRepository {

    override suspend fun findByDeviceKey(deviceKey: String): GateDevice? {
        return supabase.from("gate_device")
            .select {
                filter {
                    eq("device_key", deviceKey)
                }
            }
            .decodeSingleOrNull<GateDevice>()
    }

    override suspend fun save(device: GateDevice): GateDevice {
        return if (device.id == 0L) {
            // Insert: Supabase에 저장하고 생성된 객체 반환
            supabase.from("gate_device")
                .insert(device) {
                    select(Columns.ALL)
                }
                .decodeSingle<GateDevice>()
        } else {
            // Update: 업데이트하고 생성된 객체 반환
            supabase.from("gate_device")
                .update(device) {
                    filter {
                        eq("id", device.id)
                    }
                    select(Columns.ALL)
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

