package com.sangyoon.parkingpass.presentation.navigation.screens

import androidx.compose.runtime.Composable
import com.sangyoon.parkingpass.presentation.navigation.LocalNavigationState
import com.sangyoon.parkingpass.presentation.ui.PlateDetectionScreen
import com.sangyoon.parkingpass.presentation.viewmodel.PlateDetectionViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PlateDetectionScreenContent(parkingLotId: Long) {
    val navigationState = LocalNavigationState.current
    val viewModel = koinViewModel<PlateDetectionViewModel>()
    
    PlateDetectionScreen(
        viewModel = viewModel,
        parkingLotId = parkingLotId,
        onBack = { navigationState.pop() }
    )
}
