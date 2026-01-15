package com.sangyoon.parkingpass.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangyoon.parkingpass.domain.model.GateDirection
import com.sangyoon.parkingpass.domain.usecase.DeleteGateUseCase
import com.sangyoon.parkingpass.domain.usecase.GetGatesUseCase
import com.sangyoon.parkingpass.domain.usecase.RegisterGateUseCase
import com.sangyoon.parkingpass.domain.usecase.UpdateGateUseCase
import com.sangyoon.parkingpass.presentation.state.GateUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GateViewModel(
    private val getGatesUseCase: GetGatesUseCase,
    private val registerGateUseCase: RegisterGateUseCase,
    private val updateGateUseCase: UpdateGateUseCase,
    private val deleteGateUseCase: DeleteGateUseCase
) : ViewModel() {

    private val _selectedParkingLotId = MutableStateFlow<Long?>(null)
    val selectedParkingLotId: kotlinx.coroutines.flow.StateFlow<Long?> = _selectedParkingLotId.asStateFlow()

    private val _uiState = MutableStateFlow(GateUiState())
    val uiState = _uiState.asStateFlow()

    fun setSelectedParkingLotId(parkingLotId: Long) {
        _selectedParkingLotId.value = parkingLotId
        loadGates(parkingLotId)
    }

    fun loadGates(parkingLotId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            getGatesUseCase(parkingLotId).fold(
                onSuccess = { list ->
                    _uiState.update { it.copy(gates = list, isLoading = false) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "게이트 목록 조회 실패") }
                }
            )
        }
    }

    fun registerGate(
        parkingLotId: Long,
        name: String,
        deviceKey: String,
        direction: GateDirection,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            registerGateUseCase(parkingLotId, name, deviceKey, direction).fold(
                onSuccess = {
                    loadGates(parkingLotId)
                    onSuccess()
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "게이트 등록 실패") }
                }
            )
        }
    }

    fun updateGate(
        gateId: Long,
        parkingLotId: Long,
        name: String,
        deviceKey: String,
        direction: GateDirection,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            updateGateUseCase(gateId, parkingLotId, name, deviceKey, direction).fold(
                onSuccess = {
                    loadGates(parkingLotId)
                    onSuccess()
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "게이트 수정 실패") }
                }
            )
        }
    }

    fun deleteGate(
        gateId: Long,
        parkingLotId: Long,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            deleteGateUseCase(gateId, parkingLotId).fold(
                onSuccess = {
                    loadGates(parkingLotId)
                    onSuccess()
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "게이트 삭제 실패") }
                }
            )
        }
    }
}