package com.sangyoon.parkingpass.presentation.navigation.screens

import androidx.compose.runtime.Composable
import com.sangyoon.parkingpass.presentation.navigation.LocalNavigationState
import com.sangyoon.parkingpass.presentation.ui.CreateVehicleScreen
import com.sangyoon.parkingpass.presentation.viewmodel.VehicleViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CreateVehicleScreenContent(parkingLotId: Long) {
    val navigationState = LocalNavigationState.current
    val viewModel = koinViewModel<VehicleViewModel>()
    
    CreateVehicleScreen(
        viewModel = viewModel,
        parkingLotId = parkingLotId,
        onCreated = { navigationState.pop() },
        onBack = { navigationState.pop() }
    )
}
