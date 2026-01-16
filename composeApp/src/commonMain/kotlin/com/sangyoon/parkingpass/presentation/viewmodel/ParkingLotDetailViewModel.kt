package com.sangyoon.parkingpass.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangyoon.parkingpass.domain.usecase.GetGatesUseCase
import com.sangyoon.parkingpass.domain.usecase.GetOpenSessionsUseCase
import com.sangyoon.parkingpass.domain.usecase.GetParkingLotDetailUseCase
import com.sangyoon.parkingpass.domain.usecase.GetSessionHistoryUseCase
import com.sangyoon.parkingpass.domain.usecase.GetVehiclesUseCase
import com.sangyoon.parkingpass.presentation.state.ParkingLotDetailUiState
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class ParkingLotDetailViewModel(
    private val getParkingLotDetail: GetParkingLotDetailUseCase,
    private val getOpenSessions: GetOpenSessionsUseCase,
    private val getSessionHistory: GetSessionHistoryUseCase,
    private val getGates: GetGatesUseCase,
    private val getVehicles: GetVehiclesUseCase
) : ViewModel() {

    private val _selectedParkingLotId = MutableStateFlow<Long?>(null)
    val selectedParkingLotId: StateFlow<Long?> = _selectedParkingLotId.asStateFlow()

    private val _uiState = MutableStateFlow(ParkingLotDetailUiState())
    val uiState = _uiState.asStateFlow()

    fun setSelectedParkingLotId(parkingLotId: Long) {
        _selectedParkingLotId.value = parkingLotId
        load(parkingLotId)
    }

    @OptIn(kotlin.time.ExperimentalTime::class)
    fun load(parkingLotId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // 병렬로 데이터 로드
            val lotDeferred = async { getParkingLotDetail(parkingLotId) }
            val sessionsDeferred = async { getOpenSessions(parkingLotId) }
            val gatesDeferred = async { getGates(parkingLotId) }
            val vehiclesDeferred = async { getVehicles(parkingLotId) }

            // 오늘 날짜 가져오기
            val today = Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date
                .toString() // YYYY-MM-DD
            val todayHistoryDeferred = async { getSessionHistory(parkingLotId, today) }

            val lotResult = lotDeferred.await()
            val sessionsResult = sessionsDeferred.await()
            val gatesResult = gatesDeferred.await()
            val vehiclesResult = vehiclesDeferred.await()
            val todayHistoryResult = todayHistoryDeferred.await()

            if (lotResult.isSuccess) {
                val openSessions = sessionsResult.getOrNull() ?: emptyList()
                val todayHistory = todayHistoryResult.getOrNull() ?: emptyList()

                // 오늘의 입출차 카운트 계산
                val todayEntryCount = todayHistory.size
                val todayExitCount = todayHistory.count { it.exitedAt != null }

                // 최근 활동 (최근 3개 세션, 중복 제거)
                val recentActivities = (openSessions + todayHistory)
                    .distinctBy { it.id }
                    .sortedByDescending { it.enteredAt }
                    .take(3)

                _uiState.update {
                    it.copy(
                        parkingLot = lotResult.getOrNull(),
                        openSessions = openSessions,
                        gates = gatesResult.getOrNull() ?: emptyList(),
                        vehicles = vehiclesResult.getOrNull() ?: emptyList(),
                        todayEntryCount = todayEntryCount,
                        todayExitCount = todayExitCount,
                        recentActivities = recentActivities,
                        isLoading = false
                    )
                }
            } else {
                val err = lotResult.exceptionOrNull()
                _uiState.update { it.copy(isLoading = false, error = err?.message ?: "조회 실패") }
            }
        }
    }
}
