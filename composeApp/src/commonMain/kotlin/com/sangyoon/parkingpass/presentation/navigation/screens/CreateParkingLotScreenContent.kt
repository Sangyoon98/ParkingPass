package com.sangyoon.parkingpass.presentation.navigation.screens

import androidx.compose.runtime.Composable
import com.sangyoon.parkingpass.presentation.navigation.LocalNavigationState
import com.sangyoon.parkingpass.presentation.ui.CreateParkingLotScreen
import com.sangyoon.parkingpass.presentation.viewmodel.ParkingLotViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CreateParkingLotScreenContent() {
    val navigationState = LocalNavigationState.current
    val viewModel = koinViewModel<ParkingLotViewModel>()
    
    CreateParkingLotScreen(
        viewModel = viewModel,
        onCreated = { navigationState.pop() },
        onBack = { navigationState.pop() }
    )
}
