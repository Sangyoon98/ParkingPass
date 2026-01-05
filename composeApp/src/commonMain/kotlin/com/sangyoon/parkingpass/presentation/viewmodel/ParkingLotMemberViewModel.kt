package com.sangyoon.parkingpass.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangyoon.parkingpass.domain.model.MemberRole
import com.sangyoon.parkingpass.domain.usecase.ApproveParkingLotMemberUseCase
import com.sangyoon.parkingpass.domain.usecase.GetParkingLotMembersUseCase
import com.sangyoon.parkingpass.domain.usecase.InviteParkingLotMemberUseCase
import com.sangyoon.parkingpass.domain.usecase.RejectParkingLotMemberUseCase
import com.sangyoon.parkingpass.domain.usecase.RemoveParkingLotMemberUseCase
import com.sangyoon.parkingpass.domain.usecase.UpdateMemberRoleUseCase
import com.sangyoon.parkingpass.presentation.state.ParkingLotMemberUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ParkingLotMemberViewModel(
    private val getParkingLotMembersUseCase: GetParkingLotMembersUseCase,
    private val inviteParkingLotMemberUseCase: InviteParkingLotMemberUseCase,
    private val approveParkingLotMemberUseCase: ApproveParkingLotMemberUseCase,
    private val rejectParkingLotMemberUseCase: RejectParkingLotMemberUseCase,
    private val updateMemberRoleUseCase: UpdateMemberRoleUseCase,
    private val removeParkingLotMemberUseCase: RemoveParkingLotMemberUseCase
) : ViewModel() {

    private val _selectedParkingLotId = MutableStateFlow<Long?>(null)
    private val _uiState = MutableStateFlow(ParkingLotMemberUiState())
    val uiState: StateFlow<ParkingLotMemberUiState> = _uiState.asStateFlow()

    fun setParkingLotId(id: Long) {
        if (_selectedParkingLotId.value == id) return
        _selectedParkingLotId.value = id
        loadMembers()
    }

    fun updateInviteEmail(email: String) {
        _uiState.update { it.copy(inviteEmail = email) }
    }

    fun updateInviteRole(role: MemberRole) {
        _uiState.update { it.copy(inviteRole = role) }
    }

    fun clearMessage() {
        _uiState.update { it.copy(successMessage = null, error = null) }
    }

    fun loadMembers() {
        val parkingLotId = _selectedParkingLotId.value ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            getParkingLotMembersUseCase(parkingLotId).fold(
                onSuccess = { members ->
                    _uiState.update {
                        it.copy(
                            members = members,
                            isLoading = false
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "멤버 목록을 불러오지 못했습니다."
                        )
                    }
                }
            )
        }
    }

    fun inviteMember() {
        val parkingLotId = _selectedParkingLotId.value ?: return
        val email = _uiState.value.inviteEmail
        val role = _uiState.value.inviteRole
        if (email.isBlank()) {
            _uiState.update { it.copy(error = "초대할 이메일을 입력해주세요.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            inviteParkingLotMemberUseCase(parkingLotId, email, role).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            inviteEmail = "",
                            successMessage = "초대했습니다.",
                            isLoading = false
                        )
                    }
                    loadMembers()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "초대에 실패했습니다."
                        )
                    }
                }
            )
        }
    }

    fun approveMember(userId: String) {
        val parkingLotId = _selectedParkingLotId.value ?: return
        viewModelScope.launch {
            approveParkingLotMemberUseCase(parkingLotId, userId).fold(
                onSuccess = {
                    _uiState.update { it.copy(successMessage = "승인했습니다.") }
                    loadMembers()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(error = error.message ?: "승인에 실패했습니다.") }
                }
            )
        }
    }

    fun rejectMember(userId: String) {
        val parkingLotId = _selectedParkingLotId.value ?: return
        viewModelScope.launch {
            rejectParkingLotMemberUseCase(parkingLotId, userId).fold(
                onSuccess = {
                    _uiState.update { it.copy(successMessage = "거절했습니다.") }
                    loadMembers()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(error = error.message ?: "거절에 실패했습니다.") }
                }
            )
        }
    }

    fun changeRole(userId: String, newRole: MemberRole) {
        val parkingLotId = _selectedParkingLotId.value ?: return
        viewModelScope.launch {
            updateMemberRoleUseCase(parkingLotId, userId, newRole).fold(
                onSuccess = {
                    _uiState.update { it.copy(successMessage = "역할을 변경했습니다.") }
                    loadMembers()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(error = error.message ?: "역할 변경에 실패했습니다.") }
                }
            )
        }
    }

    fun removeMember(userId: String) {
        val parkingLotId = _selectedParkingLotId.value ?: return
        viewModelScope.launch {
            removeParkingLotMemberUseCase(parkingLotId, userId).fold(
                onSuccess = {
                    _uiState.update { it.copy(successMessage = "멤버를 제거했습니다.") }
                    loadMembers()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(error = error.message ?: "멤버 제거에 실패했습니다.") }
                }
            )
        }
    }
}
