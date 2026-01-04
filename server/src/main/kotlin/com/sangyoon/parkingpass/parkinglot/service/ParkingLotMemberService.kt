package com.sangyoon.parkingpass.parkinglot.service

import com.sangyoon.parkingpass.auth.model.User
import com.sangyoon.parkingpass.auth.repository.UserRepository
import com.sangyoon.parkingpass.parking.model.MemberRole
import com.sangyoon.parkingpass.parking.model.MemberStatus
import com.sangyoon.parkingpass.parking.model.ParkingLot
import com.sangyoon.parkingpass.parking.model.ParkingLotMember
import com.sangyoon.parkingpass.parking.repository.ParkingLotMemberRepository
import com.sangyoon.parkingpass.parking.repository.ParkingLotRepository
import java.util.UUID

data class ParkingLotMemberInfo(
    val member: ParkingLotMember,
    val user: User
)

class ParkingLotMemberService(
    private val parkingLotMemberRepository: ParkingLotMemberRepository,
    private val parkingLotRepository: ParkingLotRepository,
    private val userRepository: UserRepository
) {
    suspend fun getMembers(parkingLotId: Long): List<ParkingLotMemberInfo> {
        val members = parkingLotMemberRepository.findByParkingLotId(parkingLotId)
        if (members.isEmpty()) return emptyList()

        val users = userRepositoryMap(members.map { it.userId })
        return members.mapNotNull { member ->
            val user = users[member.userId] ?: return@mapNotNull null
            ParkingLotMemberInfo(member, user)
        }
    }

    suspend fun requestJoin(parkingLotId: Long, userId: UUID): ParkingLotMemberInfo {
        val lot = parkingLotRepository.findById(parkingLotId)
            ?: throw IllegalArgumentException("주차장을 찾을 수 없습니다.")

        val existing = parkingLotMemberRepository.findByParkingLotIdAndUserId(parkingLotId, userId)
        if (existing != null) {
            when (existing.status) {
                MemberStatus.PENDING -> throw IllegalArgumentException("이미 가입 요청이 진행 중입니다.")
                MemberStatus.APPROVED -> throw IllegalArgumentException("이미 가입된 주차장입니다.")
                MemberStatus.REJECTED -> {
                    // allow re-request by updating status to pending
                    return parkingLotMemberRepository.updateStatus(existing.id, MemberStatus.PENDING).toInfo()
                }
            }
        }

        val status = if (lot.isPublic) MemberStatus.APPROVED else MemberStatus.PENDING
        val saved = parkingLotMemberRepository.save(
            ParkingLotMember(
                parkingLotId = parkingLotId,
                userId = userId,
                role = MemberRole.MEMBER,
                status = status
            )
        )
        return saved.toInfo()
    }

    suspend fun inviteMember(
        parkingLotId: Long,
        inviterId: UUID,
        targetEmail: String,
        role: MemberRole
    ): ParkingLotMemberInfo {
        val inviterMembership = requireMembershipWithRole(parkingLotId, inviterId, MemberRole.ADMIN)
        val targetUser = userRepository.findByEmail(targetEmail.lowercase())
            ?: throw IllegalArgumentException("해당 이메일로 등록된 사용자가 없습니다.")

        val existing = parkingLotMemberRepository.findByParkingLotIdAndUserId(parkingLotId, targetUser.user.id)
        if (existing != null) {
            throw IllegalArgumentException("이미 초대되었거나 가입된 사용자입니다.")
        }

        val saved = parkingLotMemberRepository.save(
            ParkingLotMember(
                parkingLotId = parkingLotId,
                userId = targetUser.user.id,
                role = role,
                status = MemberStatus.PENDING,
                invitedBy = inviterMembership.userId
            )
        )
        return saved.toInfo(targetUser.user)
    }

    suspend fun approveMember(
        parkingLotId: Long,
        targetUserId: UUID,
        approverId: UUID
    ): ParkingLotMemberInfo {
        requireMembershipWithRole(parkingLotId, approverId, MemberRole.ADMIN)
        val member = parkingLotMemberRepository.findByParkingLotIdAndUserId(parkingLotId, targetUserId)
            ?: throw IllegalArgumentException("멤버를 찾을 수 없습니다.")
        if (member.role == MemberRole.OWNER) {
            throw IllegalArgumentException("소유자는 변경할 수 없습니다.")
        }
        return parkingLotMemberRepository.updateStatus(member.id, MemberStatus.APPROVED).toInfo()
    }

    suspend fun rejectMember(
        parkingLotId: Long,
        targetUserId: UUID,
        approverId: UUID
    ): ParkingLotMemberInfo {
        requireMembershipWithRole(parkingLotId, approverId, MemberRole.ADMIN)
        val member = parkingLotMemberRepository.findByParkingLotIdAndUserId(parkingLotId, targetUserId)
            ?: throw IllegalArgumentException("멤버를 찾을 수 없습니다.")
        if (member.role == MemberRole.OWNER) {
            throw IllegalArgumentException("소유자는 변경할 수 없습니다.")
        }
        return parkingLotMemberRepository.updateStatus(member.id, MemberStatus.REJECTED).toInfo()
    }

    suspend fun updateRole(
        parkingLotId: Long,
        targetUserId: UUID,
        approverId: UUID,
        newRole: MemberRole
    ): ParkingLotMemberInfo {
        requireMembershipWithRole(parkingLotId, approverId, MemberRole.OWNER)
        val member = parkingLotMemberRepository.findByParkingLotIdAndUserId(parkingLotId, targetUserId)
            ?: throw IllegalArgumentException("멤버를 찾을 수 없습니다.")
        if (member.role == MemberRole.OWNER) {
            throw IllegalArgumentException("소유자 역할은 변경할 수 없습니다.")
        }
        return parkingLotMemberRepository.updateRole(member.id, newRole).toInfo()
    }

    suspend fun removeMember(
        parkingLotId: Long,
        targetUserId: UUID,
        requesterId: UUID
    ) {
        val requester = requireMembershipWithRole(parkingLotId, requesterId, MemberRole.ADMIN)
        val member = parkingLotMemberRepository.findByParkingLotIdAndUserId(parkingLotId, targetUserId)
            ?: throw IllegalArgumentException("멤버를 찾을 수 없습니다.")

        if (member.role == MemberRole.OWNER) {
            throw IllegalArgumentException("소유자는 제거할 수 없습니다.")
        }
        if (requester.userId == member.userId) {
            throw IllegalArgumentException("자기 자신을 제거할 수 없습니다.")
        }
        parkingLotMemberRepository.delete(member.id)
    }

    private suspend fun requireMembershipWithRole(
        parkingLotId: Long,
        userId: UUID,
        minRole: MemberRole
    ): ParkingLotMember {
        val membership = parkingLotMemberRepository.findByParkingLotIdAndUserId(parkingLotId, userId)
            ?: throw IllegalArgumentException("주차장에 대한 권한이 없습니다.")
        if (membership.status != MemberStatus.APPROVED) {
            throw IllegalArgumentException("아직 승인되지 않은 멤버입니다.")
        }
        if (membership.role.priority < minRole.priority) {
            throw IllegalArgumentException("권한이 부족합니다.")
        }
        return membership
    }

    suspend fun ensureAccess(
        parkingLotId: Long,
        userId: UUID,
        minRole: MemberRole = MemberRole.MEMBER
    ): ParkingLotMember = requireMembershipWithRole(parkingLotId, userId, minRole)

    private fun MemberRole.priority: Int
        get() = when (this) {
            MemberRole.MEMBER -> 1
            MemberRole.ADMIN -> 2
            MemberRole.OWNER -> 3
        }

    private suspend fun userRepositoryMap(userIds: List<UUID>): Map<UUID, User> =
        userIds.distinct()
            .mapNotNull { id -> userRepository.findById(id)?.let { id to it } }
            .toMap()

    private suspend fun ParkingLotMember.toInfo(existingUser: User? = null): ParkingLotMemberInfo {
        val user = existingUser ?: userRepository.findById(this.userId)
            ?: throw IllegalStateException("사용자를 찾을 수 없습니다.")

        return ParkingLotMemberInfo(this, user)
    }
}
