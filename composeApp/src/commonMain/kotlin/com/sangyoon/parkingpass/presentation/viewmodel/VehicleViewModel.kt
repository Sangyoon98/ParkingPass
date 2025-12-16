package com.sangyoon.parkingpass.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangyoon.parkingpass.domain.usecase.CreateVehicleUseCase
import com.sangyoon.parkingpass.domain.usecase.GetVehiclesUseCase
import com.sangyoon.parkingpass.presentation.state.VehicleUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VehicleViewModel(
    private val getVehiclesUseCase: GetVehiclesUseCase,
    private val createVehicleUseCase: CreateVehicleUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(VehicleUiState())
    val uiState = _uiState.asStateFlow()

    fun loadVehicles(parkingLotId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            getVehiclesUseCase(parkingLotId).fold(
                onSuccess = { list ->
                    _uiState.update { it.copy(vehicles = list, isLoading = false) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "차량 목록 조회 실패") }
                }
            )
        }
    }

    fun createVehicle(
        parkingLotId: Long,
        plateNumber: String,
        label: String,
        category: String,
        memo: String?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            createVehicleUseCase(parkingLotId, plateNumber, label, category, memo).fold(
                onSuccess = {
                    loadVehicles(parkingLotId)
                    onSuccess()
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "차량 등록 실패") }
                }
            )
        }
    }
}