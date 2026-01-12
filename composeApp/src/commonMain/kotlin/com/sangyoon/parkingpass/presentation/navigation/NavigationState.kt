package com.sangyoon.parkingpass.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

class NavigationState {
    var selectedParkingLotId by mutableStateOf<Long?>(null)
        private set

    var lastSelectedParkingLotId by mutableStateOf<Long?>(null)
        private set

    fun setSelectedParkingLotId(id: Long) {
        selectedParkingLotId = id
        lastSelectedParkingLotId = id
        // TODO: SecureStorage에 저장하여 영속화
    }

    fun getLastSelectedParkingLotId(): Long? {
        // TODO: SecureStorage에서 불러오기
        return lastSelectedParkingLotId
    }
}

val LocalNavigationState = compositionLocalOf<NavigationState> {
    error("No NavigationState provided")
}

@Composable
fun rememberNavigationState(): NavigationState {
    return remember { NavigationState() }
}

