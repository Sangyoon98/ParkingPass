package com.sangyoon.parkingpass.common

import com.sangyoon.parkingpass.parking.model.MemberRole
import com.sangyoon.parkingpass.parkinglot.service.ParkingLotMemberService
import io.ktor.server.application.ApplicationCall
import java.util.UUID

class AuthMiddleware(
    private val parkingLotMemberService: ParkingLotMemberService
) {
    suspend fun requireParkingLotAccess(
        call: ApplicationCall,
        parkingLotId: Long,
        minRole: MemberRole = MemberRole.MEMBER
    ): UUID {
        val userId = call.requireUserId()
        parkingLotMemberService.ensureAccess(parkingLotId, userId, minRole)
        return userId
    }
}
