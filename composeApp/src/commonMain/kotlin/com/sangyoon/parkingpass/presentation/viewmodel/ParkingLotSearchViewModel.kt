package com.sangyoon.parkingpass.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangyoon.parkingpass.domain.usecase.RequestJoinParkingLotUseCase
import com.sangyoon.parkingpass.domain.usecase.SearchParkingLotsUseCase
import com.sangyoon.parkingpass.presentation.state.ParkingLotSearchUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ParkingLotSearchViewModel(
    private val searchParkingLotsUseCase: SearchParkingLotsUseCase,
    private val requestJoinParkingLotUseCase: RequestJoinParkingLotUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ParkingLotSearchUiState())
    val uiState: StateFlow<ParkingLotSearchUiState> = _uiState.asStateFlow()

    fun updateQuery(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun search() {
        val query = _uiState.value.query
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, message = null) }
            searchParkingLotsUseCase(query).fold(
                onSuccess = { lots ->
                    _uiState.update { it.copy(results = lots, isLoading = false) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "검색에 실패했습니다."
                        )
                    }
                }
            )
        }
    }

    fun requestJoin(parkingLotId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, message = null) }
            requestJoinParkingLotUseCase(parkingLotId).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            message = "가입 요청을 전송했습니다.",
                            results = it.results
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "가입 요청에 실패했습니다."
                        )
                    }
                }
            )
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}
