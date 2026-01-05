package com.sangyoon.parkingpass.presentation.state

import com.sangyoon.parkingpass.domain.model.MemberRole
import com.sangyoon.parkingpass.domain.model.ParkingLotMember

data class ParkingLotMemberUiState(
    val members: List<ParkingLotMember> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val inviteEmail: String = "",
    val inviteRole: MemberRole = MemberRole.MEMBER
)
