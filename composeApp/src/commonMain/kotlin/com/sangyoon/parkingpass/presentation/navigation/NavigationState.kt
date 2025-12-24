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

    fun setSelectedParkingLotId(id: Long) {
        selectedParkingLotId = id
    }
}

val LocalNavigationState = compositionLocalOf<NavigationState> {
    error("No NavigationState provided")
}

@Composable
fun rememberNavigationState(): NavigationState {
    return remember { NavigationState() }
}

