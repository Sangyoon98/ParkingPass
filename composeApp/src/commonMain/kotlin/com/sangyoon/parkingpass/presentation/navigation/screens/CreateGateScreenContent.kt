package com.sangyoon.parkingpass.presentation.navigation.screens

import androidx.compose.runtime.Composable
import com.sangyoon.parkingpass.presentation.navigation.LocalNavigationState
import com.sangyoon.parkingpass.presentation.ui.CreateGateScreen
import com.sangyoon.parkingpass.presentation.viewmodel.GateViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CreateGateScreenContent(parkingLotId: Long) {
    val navigationState = LocalNavigationState.current
    val viewModel = koinViewModel<GateViewModel>()
    
    CreateGateScreen(
        viewModel = viewModel,
        parkingLotId = parkingLotId,
        onCreated = { navigationState.pop() },
        onBack = { navigationState.pop() }
    )
}
