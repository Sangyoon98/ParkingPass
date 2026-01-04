package com.sangyoon.parkingpass.parking.repository

import com.sangyoon.parkingpass.parking.model.MemberRole
import com.sangyoon.parkingpass.parking.model.MemberStatus
import com.sangyoon.parkingpass.parking.model.ParkingLotMember
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import java.util.UUID

class SupabaseParkingLotMemberRepository(
    private val supabase: SupabaseClient
) : ParkingLotMemberRepository {

    override suspend fun findByParkingLotId(parkingLotId: Long): List<ParkingLotMember> {
        return supabase.from("parking_lot_member")
            .select {
                filter {
                    eq("parking_lot_id", parkingLotId)
                }
                order("joined_at", ascending = false)
            }
            .decodeList<ParkingLotMember>()
    }

    override suspend fun findByUserId(userId: UUID): List<ParkingLotMember> {
        return supabase.from("parking_lot_member")
            .select {
                filter {
                    eq("user_id", userId)
                }
            }
            .decodeList<ParkingLotMember>()
    }

    override suspend fun findByParkingLotIdAndUserId(
        parkingLotId: Long,
        userId: UUID
    ): ParkingLotMember? {
        return supabase.from("parking_lot_member")
            .select {
                filter {
                    eq("parking_lot_id", parkingLotId)
                    eq("user_id", userId)
                }
                limit(1)
            }
            .decodeSingleOrNull<ParkingLotMember>()
    }

    override suspend fun save(member: ParkingLotMember): ParkingLotMember {
        return supabase.from("parking_lot_member")
            .insert(member) {
                select(Columns.ALL)
            }
            .decodeSingle<ParkingLotMember>()
    }

    override suspend fun updateStatus(memberId: Long, status: MemberStatus): ParkingLotMember {
        return supabase.from("parking_lot_member")
            .update(
                mapOf("status" to status.name)
            ) {
                filter {
                    eq("id", memberId)
                }
                select(Columns.ALL)
            }
            .decodeSingle<ParkingLotMember>()
    }

    override suspend fun updateRole(memberId: Long, role: MemberRole): ParkingLotMember {
        return supabase.from("parking_lot_member")
            .update(
                mapOf("role" to role.name)
            ) {
                filter {
                    eq("id", memberId)
                }
                select(Columns.ALL)
            }
            .decodeSingle<ParkingLotMember>()
    }

    override suspend fun delete(memberId: Long) {
        supabase.from("parking_lot_member")
            .delete {
                filter {
                    eq("id", memberId)
                }
            }
    }
}
