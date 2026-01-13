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

    private var _lastSelectedParkingLotId by mutableStateOf<Long?>(null)
    val lastSelectedParkingLotId: Long?
        get() {
            return _lastSelectedParkingLotId
        }

    fun setSelectedParkingLotId(id: Long) {
        selectedParkingLotId = id
        _lastSelectedParkingLotId = id
        // TODO: SecureStorage에 저장하여 영속화
    }
}

val LocalNavigationState = compositionLocalOf<NavigationState> {
    error("No NavigationState provided")
}

@Composable
fun rememberNavigationState(): NavigationState {
    return remember { NavigationState() }
}

