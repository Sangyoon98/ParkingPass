package com.sangyoon.parkingpass.presentation.navigation.screens

import androidx.compose.runtime.Composable
import com.sangyoon.parkingpass.presentation.navigation.LocalNavigationState
import com.sangyoon.parkingpass.presentation.navigation.Screen
import com.sangyoon.parkingpass.presentation.ui.GateListScreen
import com.sangyoon.parkingpass.presentation.viewmodel.GateViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun GateListScreenContent(parkingLotId: Long) {
    val navigationState = LocalNavigationState.current
    val viewModel = koinViewModel<GateViewModel>()
    
    GateListScreen(
        viewModel = viewModel,
        parkingLotId = parkingLotId,
        onBack = { navigationState.pop() },
        onCreateClick = { navigationState.push(Screen.CreateGate(parkingLotId)) }
    )
}
