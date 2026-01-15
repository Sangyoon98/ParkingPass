package com.sangyoon.parkingpass.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangyoon.parkingpass.domain.model.VehicleCategory
import com.sangyoon.parkingpass.domain.usecase.CreateVehicleUseCase
import com.sangyoon.parkingpass.domain.usecase.DeleteVehicleUseCase
import com.sangyoon.parkingpass.domain.usecase.GetVehiclesUseCase
import com.sangyoon.parkingpass.domain.usecase.UpdateVehicleUseCase
import com.sangyoon.parkingpass.presentation.state.VehicleUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VehicleViewModel(
    private val getVehiclesUseCase: GetVehiclesUseCase,
    private val createVehicleUseCase: CreateVehicleUseCase,
    private val updateVehicleUseCase: UpdateVehicleUseCase,
    private val deleteVehicleUseCase: DeleteVehicleUseCase
) : ViewModel() {

    private val _selectedParkingLotId = MutableStateFlow<Long?>(null)
    val selectedParkingLotId: kotlinx.coroutines.flow.StateFlow<Long?> = _selectedParkingLotId.asStateFlow()

    private val _uiState = MutableStateFlow(VehicleUiState())
    val uiState = _uiState.asStateFlow()

    fun setSelectedParkingLotId(parkingLotId: Long) {
        _selectedParkingLotId.value = parkingLotId
        loadVehicles(parkingLotId)
    }

    fun loadVehicles(parkingLotId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            getVehiclesUseCase(parkingLotId).fold(
                onSuccess = { list ->
                    _uiState.update { it.copy(vehicles = list, isLoading = false) }
                },
                onFailure = {
                    // 목록 조회 에러는 UI에 에러 메시지를 띄우지 않고, 그냥 로딩만 종료
                    _uiState.update { it.copy(isLoading = false) }
                }
            )
        }
    }

    fun createVehicle(
        parkingLotId: Long,
        plateNumber: String,
        label: String,
        category: VehicleCategory,
        memo: String?
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, creationSuccess = false) }
            createVehicleUseCase(parkingLotId, plateNumber, label, category, memo).fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, creationSuccess = true) }
                    loadVehicles(parkingLotId)
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "차량 등록 실패") }
                }
            )
        }
    }

    fun updateVehicle(
        vehicleId: Long,
        parkingLotId: Long,
        label: String,
        category: VehicleCategory,
        memo: String?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            updateVehicleUseCase(vehicleId, parkingLotId, label, category, memo).fold(
                onSuccess = {
                    loadVehicles(parkingLotId)
                    onSuccess()
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "차량 수정 실패") }
                }
            )
        }
    }

    fun deleteVehicle(
        vehicleId: Long,
        parkingLotId: Long,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            deleteVehicleUseCase(vehicleId, parkingLotId).fold(
                onSuccess = {
                    loadVehicles(parkingLotId)
                    onSuccess()
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "차량 삭제 실패") }
                }
            )
        }
    }
}