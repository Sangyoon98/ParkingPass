package com.sangyoon.parkingpass.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangyoon.parkingpass.domain.usecase.GetParkingLotsUseCase
import com.sangyoon.parkingpass.domain.usecase.CreateParkingLotUseCase
import com.sangyoon.parkingpass.presentation.state.ParkingLotUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ParkingLotViewModel(
    private val getParkingLotsUseCase: GetParkingLotsUseCase,
    private val createParkingLotUseCase: CreateParkingLotUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(ParkingLotUiState())
    val uiState: StateFlow<ParkingLotUiState> = _uiState.asStateFlow()

    init {
        loadParkingLots()
    }

    fun loadParkingLots() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            getParkingLotsUseCase().fold(
                onSuccess = { parkingLots ->
                    _uiState.update {
                        it.copy(
                            parkingLots = parkingLots,
                            isLoading = false
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "알 수 없는 오류가 발생했습니다"
                        )
                    }
                }
            )
        }
    }

    fun createParkingLot(name: String, location: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            createParkingLotUseCase(name, location).fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, creationSuccess = true) }
                    loadParkingLots()   // 목록 새로고침
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "주차장 생성에 실패했습니다"
                        )
                    }
                }
            )
        }
    }

    fun resetCreationSuccess() {
        _uiState.update { it.copy(creationSuccess = false) }
    }
}