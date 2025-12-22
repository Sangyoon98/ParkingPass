package com.sangyoon.parkingpass.presentation.navigation.screens

import androidx.compose.runtime.Composable
import com.sangyoon.parkingpass.presentation.navigation.LocalNavigationState
import com.sangyoon.parkingpass.presentation.ui.PlateDetectionScreen
import com.sangyoon.parkingpass.presentation.viewmodel.PlateDetectionViewModel
import org.koin.compose.getKoin

@Composable
fun PlateDetectionScreenContent(parkingLotId: Long) {
    val navigationState = LocalNavigationState.current
    val viewModel = getKoin().get<PlateDetectionViewModel>()
    
    PlateDetectionScreen(
        viewModel = viewModel,
        parkingLotId = parkingLotId,
        onBack = { navigationState.pop() }
    )
}
