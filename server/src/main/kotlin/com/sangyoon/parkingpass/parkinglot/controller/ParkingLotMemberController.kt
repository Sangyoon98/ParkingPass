package com.sangyoon.parkingpass.parkinglot.controller

import com.sangyoon.parkingpass.common.requireUserId
import com.sangyoon.parkingpass.parking.model.MemberRole
import com.sangyoon.parkingpass.parkinglot.dto.InviteMemberRequest
import com.sangyoon.parkingpass.parkinglot.dto.ParkingLotMemberResponse
import com.sangyoon.parkingpass.parkinglot.dto.UpdateMemberRoleRequest
import com.sangyoon.parkingpass.parkinglot.service.ParkingLotMemberInfo
import com.sangyoon.parkingpass.parkinglot.service.ParkingLotMemberService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import java.util.UUID

private fun ParkingLotMemberInfo.toResponse() = ParkingLotMemberResponse(
    id = member.id,
    userId = user.id.toString(),
    email = user.email,
    name = user.name,
    role = member.role,
    status = member.status,
    invitedBy = member.invitedBy?.toString(),
    joinedAt = member.joinedAt?.toString()
)

fun Route.parkingLotMemberController(
    memberService: ParkingLotMemberService
) {
    authenticate("auth-jwt") {
        route("/api/v1/parking-lots/{parkingLotId}/members") {
            get {
                val parkingLotId = call.parameters["parkingLotId"]?.toLongOrNull()
                    ?: throw IllegalArgumentException("parkingLotId가 필요합니다.")
                val userId = call.requireUserId()
                memberService.ensureAccess(parkingLotId, userId, MemberRole.MEMBER)

                val members = memberService.getMembers(parkingLotId)
                call.respond(HttpStatusCode.OK, members.map { it.toResponse() })
            }

            post("/join-request") {
                val parkingLotId = call.parameters["parkingLotId"]?.toLongOrNull()
                    ?: throw IllegalArgumentException("parkingLotId가 필요합니다.")
                val userId = call.requireUserId()
                val result = memberService.requestJoin(parkingLotId, userId)
                call.respond(HttpStatusCode.OK, result.toResponse())
            }

            post("/invite") {
                val parkingLotId = call.parameters["parkingLotId"]?.toLongOrNull()
                    ?: throw IllegalArgumentException("parkingLotId가 필요합니다.")
                val inviterId = call.requireUserId()
                val request = call.receive<InviteMemberRequest>()
                val result = memberService.inviteMember(
                    parkingLotId = parkingLotId,
                    inviterId = inviterId,
                    targetEmail = request.email,
                    role = request.role
                )
                call.respond(HttpStatusCode.Created, result.toResponse())
            }

            put("/{userId}/approve") {
                val parkingLotId = call.parameters["parkingLotId"]?.toLongOrNull()
                    ?: throw IllegalArgumentException("parkingLotId가 필요합니다.")
                val targetUserId = call.parameters["userId"]?.let(UUID::fromString)
                    ?: throw IllegalArgumentException("userId가 필요합니다.")
                val approverId = call.requireUserId()
                val member = memberService.approveMember(parkingLotId, targetUserId, approverId)
                call.respond(HttpStatusCode.OK, member.toResponse())
            }

            put("/{userId}/reject") {
                val parkingLotId = call.parameters["parkingLotId"]?.toLongOrNull()
                    ?: throw IllegalArgumentException("parkingLotId가 필요합니다.")
                val targetUserId = call.parameters["userId"]?.let(UUID::fromString)
                    ?: throw IllegalArgumentException("userId가 필요합니다.")
                val approverId = call.requireUserId()
                val member = memberService.rejectMember(parkingLotId, targetUserId, approverId)
                call.respond(HttpStatusCode.OK, member.toResponse())
            }

            put("/{userId}/role") {
                val parkingLotId = call.parameters["parkingLotId"]?.toLongOrNull()
                    ?: throw IllegalArgumentException("parkingLotId가 필요합니다.")
                val targetUserId = call.parameters["userId"]?.let(UUID::fromString)
                    ?: throw IllegalArgumentException("userId가 필요합니다.")
                val approverId = call.requireUserId()
                val request = call.receive<UpdateMemberRoleRequest>()
                val member = memberService.updateRole(parkingLotId, targetUserId, approverId, request.role)
                call.respond(HttpStatusCode.OK, member.toResponse())
            }

            delete("/{userId}") {
                val parkingLotId = call.parameters["parkingLotId"]?.toLongOrNull()
                    ?: throw IllegalArgumentException("parkingLotId가 필요합니다.")
                val targetUserId = call.parameters["userId"]?.let(UUID::fromString)
                    ?: throw IllegalArgumentException("userId가 필요합니다.")
                val requesterId = call.requireUserId()
                memberService.removeMember(parkingLotId, targetUserId, requesterId)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}
