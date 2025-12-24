package com.sangyoon.parkingpass.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangyoon.parkingpass.domain.model.Gate
import com.sangyoon.parkingpass.domain.usecase.GetGatesUseCase
import com.sangyoon.parkingpass.domain.usecase.PlateDetectedUseCase
import com.sangyoon.parkingpass.presentation.state.PlateDetectionUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlateDetectionViewModel(
    private val getGatesUseCase: GetGatesUseCase,
    private val plateDetectedUseCase: PlateDetectedUseCase
): ViewModel() {

    private val _selectedParkingLotId = MutableStateFlow<Long?>(null)
    val selectedParkingLotId: kotlinx.coroutines.flow.StateFlow<Long?> = _selectedParkingLotId.asStateFlow()

    private val _uiState = MutableStateFlow(PlateDetectionUiState())
    val uiState = _uiState.asStateFlow()

    fun setSelectedParkingLotId(parkingLotId: Long) {
        _selectedParkingLotId.value = parkingLotId
        loadGates(parkingLotId)
    }

    fun loadGates(parkingLotId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            getGatesUseCase(parkingLotId).fold(
                onSuccess = { gates ->
                    _uiState.update { it.copy(gates = gates, isLoading = false) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "게이트 목록 조회 실패")}
                }
            )
        }
    }

    fun selectGate(gate: Gate) {
        _uiState.update { it.copy(selectedGate = gate) }
    }

    fun updatePlateNumber(plateNumber: String) {
        _uiState.update { it.copy(plateNumber = plateNumber) }
    }

    fun detectPlate(onSuccess: () -> Unit) {
        val selectedGate = _uiState.value.selectedGate
        val plateNumber = _uiState.value.plateNumber

        if (selectedGate == null) {
            _uiState.update { it.copy(error = "게이트를 선택해주세요") }
            return
        }

        if (plateNumber.isBlank()) {
            _uiState.update { it.copy(error = "번호판 번호를 입력해주세요") }
            return
        }

        if (_uiState.value.isDetecting) {
            return  // 이미 감지 중이면 중복 요청 방지
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isDetecting = true, error = null, result = null) }
            plateDetectedUseCase(selectedGate.deviceKey, plateNumber.trim()).fold(
                onSuccess = { response ->
                    _uiState.update {
                        it.copy(
                            isDetecting = false,
                            result = response,
                            plateNumber = ""  // 성공 후 번호판 초기화
                        )
                    }
                    onSuccess()
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isDetecting = false,
                            error = e.message ?: "입출차 체크 실패"
                        )
                    }
                }
            )
        }
    }

    fun clearResult() {
        _uiState.update { it.copy(result = null) }
    }
}