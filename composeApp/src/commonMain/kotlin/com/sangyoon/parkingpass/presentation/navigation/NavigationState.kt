package com.sangyoon.parkingpass.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * 상태 기반 네비게이션을 위한 Screen 정의
 */
sealed class Screen {
    object ParkingLotList : Screen()
    object CreateParkingLot : Screen()
    data class ParkingLotDetail(val parkingLotId: Long) : Screen()
    data class VehicleList(val parkingLotId: Long) : Screen()
    data class CreateVehicle(val parkingLotId: Long) : Screen()
    data class GateList(val parkingLotId: Long) : Screen()
    data class CreateGate(val parkingLotId: Long) : Screen()
    data class PlateDetection(val parkingLotId: Long) : Screen()
    data class SessionList(val parkingLotId: Long) : Screen()
}

/**
 * 네비게이션 스택 관리
 */
class NavigationState {
    val backStack = mutableStateOf<List<Screen>>(listOf(Screen.ParkingLotList))
    val currentScreen: Screen get() = backStack.value.lastOrNull() ?: Screen.ParkingLotList
    
    fun push(screen: Screen) {
        backStack.value = backStack.value + screen
    }
    
    fun pop(): Boolean {
        return if (backStack.value.size > 1) {
            backStack.value = backStack.value.dropLast(1)
            true
        } else {
            false
        }
    }
    
    fun replace(screen: Screen) {
        backStack.value = backStack.value.dropLast(1) + screen
    }
    
    fun popToRoot() {
        backStack.value = listOf(Screen.ParkingLotList)
    }
}

@Composable
fun rememberNavigationState(): NavigationState {
    return remember { NavigationState() }
}

