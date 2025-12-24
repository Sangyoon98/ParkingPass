package com.sangyoon.parkingpass.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangyoon.parkingpass.domain.usecase.GetOpenSessionsUseCase
import com.sangyoon.parkingpass.domain.usecase.GetSessionHistoryUseCase
import com.sangyoon.parkingpass.presentation.state.SessionUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SessionViewModel(
    private val getOpenSessions: GetOpenSessionsUseCase,
    private val getSessionHistory: GetSessionHistoryUseCase
) : ViewModel() {

    private val _selectedParkingLotId = MutableStateFlow<Long?>(null)
    val selectedParkingLotId: kotlinx.coroutines.flow.StateFlow<Long?> = _selectedParkingLotId.asStateFlow()

    private val _uiState = MutableStateFlow(SessionUiState())
    val uiState = _uiState.asStateFlow()

    fun setSelectedParkingLotId(parkingLotId: Long) {
        _selectedParkingLotId.value = parkingLotId
        loadSessions(parkingLotId)
    }

    fun loadSessions(parkingLotId: Long, date: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val openResult = getOpenSessions(parkingLotId)
            val historyResult = if (date != null) {
                getSessionHistory(parkingLotId, date)
            } else {
                kotlin.Result.success(emptyList())
            }

            if (openResult.isSuccess && historyResult.isSuccess) {
                _uiState.update {
                    it.copy(
                        openSessions = openResult.getOrDefault(emptyList()),
                        history = historyResult.getOrDefault(emptyList()),
                        selectedDate = date,
                        isLoading = false
                    )
                }
            } else {
                val err = openResult.exceptionOrNull() ?: historyResult.exceptionOrNull()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = err?.message ?: "세션 조회 실패"
                    )
                }
            }
        }
    }
}