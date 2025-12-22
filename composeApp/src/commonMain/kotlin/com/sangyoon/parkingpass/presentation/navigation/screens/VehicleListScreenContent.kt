package com.sangyoon.parkingpass.presentation.navigation.screens

import androidx.compose.runtime.Composable
import com.sangyoon.parkingpass.presentation.navigation.LocalNavigationState
import com.sangyoon.parkingpass.presentation.navigation.Screen
import com.sangyoon.parkingpass.presentation.ui.VehicleListScreen
import com.sangyoon.parkingpass.presentation.viewmodel.VehicleViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun VehicleListScreenContent(parkingLotId: Long) {
    val navigationState = LocalNavigationState.current
    val viewModel = koinViewModel<VehicleViewModel>()
    
    VehicleListScreen(
        viewModel = viewModel,
        parkingLotId = parkingLotId,
        onBack = { navigationState.pop() },
        onCreateClick = { navigationState.push(Screen.CreateVehicle(parkingLotId)) }
    )
}
