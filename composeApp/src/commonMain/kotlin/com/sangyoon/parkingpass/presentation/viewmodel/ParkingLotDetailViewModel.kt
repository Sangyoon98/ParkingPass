package com.sangyoon.parkingpass.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangyoon.parkingpass.domain.usecase.GetOpenSessionsUseCase
import com.sangyoon.parkingpass.domain.usecase.GetParkingLotDetailUseCase
import com.sangyoon.parkingpass.domain.usecase.GetSessionHistoryUseCase
import com.sangyoon.parkingpass.presentation.state.ParkingLotDetailUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ParkingLotDetailViewModel(
    private val getParkingLotDetail: GetParkingLotDetailUseCase,
    private val getOpenSessions: GetOpenSessionsUseCase,
    private val getSessionHistory: GetSessionHistoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ParkingLotDetailUiState())
    val uiState = _uiState.asStateFlow()

    fun load(parkingLotId: Long, date: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val lotResult = getParkingLotDetail(parkingLotId)
            val openResult = getOpenSessions(parkingLotId)
            val historyResult = if (date != null) getSessionHistory(parkingLotId, date) else Result.success(emptyList())

            if (lotResult.isSuccess && openResult.isSuccess && historyResult.isSuccess) {
                _uiState.update {
                    it.copy(
                        parkingLot = lotResult.getOrNull(),
                        openSessions = openResult.getOrDefault(emptyList()),
                        history = historyResult.getOrDefault(emptyList()),
                        selectedDate = date,
                        isLoading = false
                    )
                }
            } else {
                val err = lotResult.exceptionOrNull() ?: openResult.exceptionOrNull() ?: historyResult.exceptionOrNull()
                _uiState.update { it.copy(isLoading = false, error = err?.message ?: "조회 실패") }
            }
        }
    }
}