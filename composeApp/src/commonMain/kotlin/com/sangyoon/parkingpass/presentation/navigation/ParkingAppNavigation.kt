package com.sangyoon.parkingpass.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import com.sangyoon.parkingpass.presentation.navigation.screens.*

/**
 * NavigationState를 CompositionLocal로 제공
 */
val LocalNavigationState = compositionLocalOf<NavigationState> {
    error("NavigationState not provided")
}

@Composable
expect fun BackHandler(enabled: Boolean, onBack: () -> Unit)

@Composable
fun ParkingAppNavigation() {
    val navigationState = rememberNavigationState()
    val backStack by navigationState.backStack
    val currentScreen = backStack.lastOrNull() ?: Screen.ParkingLotList
    
    // Android 뒤로가기 버튼 처리
    BackHandler(enabled = backStack.size > 1) {
        navigationState.pop()
    }
    
    CompositionLocalProvider(LocalNavigationState provides navigationState) {
        when (val screen = currentScreen) {
            is Screen.ParkingLotList -> ParkingLotListScreenContent()
            is Screen.CreateParkingLot -> CreateParkingLotScreenContent()
            is Screen.ParkingLotDetail -> ParkingLotDetailScreenContent(screen.parkingLotId)
            is Screen.VehicleList -> VehicleListScreenContent(screen.parkingLotId)
            is Screen.CreateVehicle -> CreateVehicleScreenContent(screen.parkingLotId)
            is Screen.GateList -> GateListScreenContent(screen.parkingLotId)
            is Screen.CreateGate -> CreateGateScreenContent(screen.parkingLotId)
            is Screen.PlateDetection -> PlateDetectionScreenContent(screen.parkingLotId)
            is Screen.SessionList -> SessionListScreenContent(screen.parkingLotId)
        }
    }
}
