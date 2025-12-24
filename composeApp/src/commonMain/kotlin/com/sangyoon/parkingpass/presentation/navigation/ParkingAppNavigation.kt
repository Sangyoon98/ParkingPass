package com.sangyoon.parkingpass.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.sangyoon.parkingpass.presentation.ui.CreateGateScreen
import com.sangyoon.parkingpass.presentation.ui.CreateParkingLotScreen
import com.sangyoon.parkingpass.presentation.ui.CreateVehicleScreen
import com.sangyoon.parkingpass.presentation.ui.GateListScreen
import com.sangyoon.parkingpass.presentation.ui.ParkingLotDetailScreen
import com.sangyoon.parkingpass.presentation.ui.ParkingLotListScreen
import com.sangyoon.parkingpass.presentation.ui.PlateDetectionScreen
import com.sangyoon.parkingpass.presentation.ui.SessionListScreen
import com.sangyoon.parkingpass.presentation.ui.VehicleListScreen
import com.sangyoon.parkingpass.presentation.viewmodel.GateViewModel
import com.sangyoon.parkingpass.presentation.viewmodel.ParkingLotDetailViewModel
import com.sangyoon.parkingpass.presentation.viewmodel.ParkingLotViewModel
import com.sangyoon.parkingpass.presentation.viewmodel.PlateDetectionViewModel
import com.sangyoon.parkingpass.presentation.viewmodel.SessionViewModel
import com.sangyoon.parkingpass.presentation.viewmodel.VehicleViewModel
import org.koin.compose.viewmodel.koinViewModel

sealed class Screen {
    object ParkingLotList : Screen()
    data class ParkingLotDetail(val parkingLotId: Long) : Screen()
    object CreateParkingLot : Screen()
    data class VehicleList(val parkingLotId: Long) : Screen()
    data class CreateVehicle(val parkingLotId: Long) : Screen()
    data class GateList(val parkingLotId: Long) : Screen()
    data class CreateGate(val parkingLotId: Long) : Screen()
    data class PlateDetection(val parkingLotId: Long) : Screen()
    data class SessionList(val parkingLotId: Long) : Screen()
}

@Composable
fun ParkingAppNavigation() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.ParkingLotList) }

    when (val screen = currentScreen) {
        is Screen.ParkingLotList -> {
            val viewModel = koinViewModel<ParkingLotViewModel>()
            ParkingLotListScreen(
                viewModel = viewModel,
                onParkingLotClick = { parkingLotId ->
                    currentScreen = Screen.ParkingLotDetail(parkingLotId)
                },
                onCreateClick = {
                    currentScreen = Screen.CreateParkingLot
                }
            )
        }

        is Screen.CreateParkingLot -> {
            val viewModel = koinViewModel<ParkingLotViewModel>()
            CreateParkingLotScreen(
                viewModel = viewModel,
                onCreated = { currentScreen = Screen.ParkingLotList },
                onBack = { currentScreen = Screen.ParkingLotList }
            )
        }

        is Screen.ParkingLotDetail -> {
            val viewModel = koinViewModel<ParkingLotDetailViewModel>()
            ParkingLotDetailScreen(
                viewModel = viewModel,
                parkingLotId = screen.parkingLotId,
                onCreateVehicleClick = {
                    currentScreen = Screen.VehicleList(screen.parkingLotId)
                },
                onManageGateClick = {
                    currentScreen = Screen.GateList(screen.parkingLotId)
                },
                onPlateDetectionClick = {
                    currentScreen = Screen.PlateDetection(screen.parkingLotId)
                },
                onSessionListClick = {
                    currentScreen = Screen.SessionList(screen.parkingLotId)
                },
                onBack = { currentScreen = Screen.ParkingLotList }
            )
        }

        is Screen.VehicleList -> {
            val viewModel = koinViewModel<VehicleViewModel>()
            VehicleListScreen(
                viewModel = viewModel,
                parkingLotId = screen.parkingLotId,
                onBack = { currentScreen = Screen.ParkingLotDetail(screen.parkingLotId) },
                onCreateClick = {
                    currentScreen = Screen.CreateVehicle(screen.parkingLotId)
                }
            )
        }

        is Screen.CreateVehicle -> {
            val viewModel = koinViewModel<VehicleViewModel>()
            CreateVehicleScreen(
                viewModel = viewModel,
                parkingLotId = screen.parkingLotId,
                onCreated = {
                    currentScreen = Screen.VehicleList(screen.parkingLotId)
                },
                onBack = { currentScreen = Screen.VehicleList(screen.parkingLotId) }
            )
        }

        is Screen.GateList -> {
            val viewModel = koinViewModel<GateViewModel>()
            GateListScreen(
                viewModel = viewModel,
                parkingLotId = screen.parkingLotId,
                onBack = { currentScreen = Screen.ParkingLotDetail(screen.parkingLotId) },
                onCreateClick = {
                    currentScreen = Screen.CreateGate(screen.parkingLotId)
                }
            )
        }

        is Screen.CreateGate -> {
            val viewModel = koinViewModel<GateViewModel>()
            CreateGateScreen(
                viewModel = viewModel,
                parkingLotId = screen.parkingLotId,
                onCreated = {
                    currentScreen = Screen.GateList(screen.parkingLotId)
                },
                onBack = { currentScreen = Screen.GateList(screen.parkingLotId) }
            )
        }

        is Screen.PlateDetection -> {
            val viewModel = koinViewModel<PlateDetectionViewModel>()
            PlateDetectionScreen(
                viewModel = viewModel,
                parkingLotId = screen.parkingLotId,
                onBack = { currentScreen = Screen.ParkingLotDetail(screen.parkingLotId) }
            )
        }

        is Screen.SessionList -> {
            val viewModel = koinViewModel<SessionViewModel>()
            SessionListScreen(
                viewModel = viewModel,
                parkingLotId = screen.parkingLotId,
                onBack = { currentScreen = Screen.ParkingLotDetail(screen.parkingLotId) }
            )
        }
    }
}

