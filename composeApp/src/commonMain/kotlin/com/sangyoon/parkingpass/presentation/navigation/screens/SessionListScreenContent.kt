package com.sangyoon.parkingpass.presentation.navigation.screens

import androidx.compose.runtime.Composable
import com.sangyoon.parkingpass.presentation.navigation.LocalNavigationState
import com.sangyoon.parkingpass.presentation.ui.SessionListScreen
import com.sangyoon.parkingpass.presentation.viewmodel.SessionViewModel
import org.koin.compose.getKoin

@Composable
fun SessionListScreenContent(parkingLotId: Long) {
    val navigationState = LocalNavigationState.current
    val viewModel = getKoin().get<SessionViewModel>()
    
    SessionListScreen(
        viewModel = viewModel,
        parkingLotId = parkingLotId,
        onBack = { navigationState.pop() }
    )
}
