package com.sangyoon.parkingpass.presentation.navigation.screens

import androidx.compose.runtime.Composable
import com.sangyoon.parkingpass.presentation.navigation.LocalNavigationState
import com.sangyoon.parkingpass.presentation.ui.CreateVehicleScreen
import com.sangyoon.parkingpass.presentation.viewmodel.VehicleViewModel
import org.koin.compose.getKoin

@Composable
fun CreateVehicleScreenContent(parkingLotId: Long) {
    val navigationState = LocalNavigationState.current
    val viewModel = getKoin().get<VehicleViewModel>()
    
    CreateVehicleScreen(
        viewModel = viewModel,
        parkingLotId = parkingLotId,
        onCreated = { navigationState.pop() },
        onBack = { navigationState.pop() }
    )
}
