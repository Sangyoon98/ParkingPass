package com.sangyoon.parkingpass.presentation.navigation.screens

import androidx.compose.runtime.Composable
import com.sangyoon.parkingpass.presentation.navigation.LocalNavigationState
import com.sangyoon.parkingpass.presentation.navigation.Screen
import com.sangyoon.parkingpass.presentation.ui.ParkingLotListScreen
import com.sangyoon.parkingpass.presentation.viewmodel.ParkingLotViewModel
import org.koin.compose.getKoin

@Composable
fun ParkingLotListScreenContent() {
    val navigationState = LocalNavigationState.current
    val viewModel = getKoin().get<ParkingLotViewModel>()
    
    ParkingLotListScreen(
        viewModel = viewModel,
        onParkingLotClick = { parkingLotId ->
            navigationState.push(Screen.ParkingLotDetail(parkingLotId))
        },
        onCreateClick = {
            navigationState.push(Screen.CreateParkingLot)
        }
    )
}
