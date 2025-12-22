package com.sangyoon.parkingpass.presentation.navigation.screens

import androidx.compose.runtime.Composable
import com.sangyoon.parkingpass.presentation.navigation.LocalNavigationState
import com.sangyoon.parkingpass.presentation.navigation.Screen
import com.sangyoon.parkingpass.presentation.ui.ParkingLotDetailScreen
import com.sangyoon.parkingpass.presentation.viewmodel.ParkingLotDetailViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ParkingLotDetailScreenContent(parkingLotId: Long) {
    val navigationState = LocalNavigationState.current
    val viewModel = koinViewModel<ParkingLotDetailViewModel>()
    
    ParkingLotDetailScreen(
        viewModel = viewModel,
        parkingLotId = parkingLotId,
        onCreateVehicleClick = {
            navigationState.push(Screen.VehicleList(parkingLotId))
        },
        onManageGateClick = {
            navigationState.push(Screen.GateList(parkingLotId))
        },
        onPlateDetectionClick = {
            navigationState.push(Screen.PlateDetection(parkingLotId))
        },
        onSessionListClick = {
            navigationState.push(Screen.SessionList(parkingLotId))
        }
    )
}
