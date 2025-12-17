package com.sangyoon.parkingpass.presentation.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sangyoon.parkingpass.presentation.ui.CreateGateScreen
import com.sangyoon.parkingpass.presentation.ui.CreateParkingLotScreen
import com.sangyoon.parkingpass.presentation.ui.CreateVehicleScreen
import com.sangyoon.parkingpass.presentation.ui.GateListScreen
import com.sangyoon.parkingpass.presentation.ui.ParkingLotDetailScreen
import com.sangyoon.parkingpass.presentation.ui.ParkingLotListScreen
import com.sangyoon.parkingpass.presentation.ui.PlateDetectionScreen
import com.sangyoon.parkingpass.presentation.ui.VehicleListScreen
import com.sangyoon.parkingpass.presentation.viewmodel.GateViewModel
import com.sangyoon.parkingpass.presentation.viewmodel.ParkingLotDetailViewModel
import com.sangyoon.parkingpass.presentation.viewmodel.ParkingLotViewModel
import com.sangyoon.parkingpass.presentation.viewmodel.PlateDetectionViewModel
import com.sangyoon.parkingpass.presentation.viewmodel.VehicleViewModel
import org.koin.compose.getKoin

sealed class Screen(val route: String) {
    object ParkingLotList : Screen("parking_lot_list")
    object ParkingLotDetail : Screen("parking_lot_detail")
    object CreateParkingLot : Screen("create_parking_lot")
    object VehicleList : Screen("vehicle_list")
    object CreateVehicle : Screen("create_vehicle")
    object GateList : Screen("gate_list")
    object CreateGate : Screen("create_gate")
    object PlateDetection : Screen("plate_detection")
}

@Composable
fun ParkingAppNavigation(
    navController: NavHostController = rememberNavController()
) {
    var selectedParkingLotId by remember { mutableStateOf<Long?>(null) }

    NavHost(
        navController = navController,
        startDestination = Screen.ParkingLotList.route
    ) {
        composable(Screen.ParkingLotList.route) {
            val koin = getKoin()
            val viewModel: ParkingLotViewModel = remember { koin.get<ParkingLotViewModel>() }
            ParkingLotListScreen(
                viewModel = viewModel,
                onParkingLotClick = { parkingLotId ->
                    selectedParkingLotId = parkingLotId
                    navController.navigate(Screen.ParkingLotDetail.route)
                },
                onCreateClick = {
                    navController.navigate(Screen.CreateParkingLot.route)
                }
            )
        }

        composable(Screen.CreateParkingLot.route) {
            val koin = getKoin()
            val viewModel: ParkingLotViewModel = remember { koin.get<ParkingLotViewModel>() }
            CreateParkingLotScreen(
                viewModel = viewModel,
                onCreated = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ParkingLotDetail.route) {
            val koin = getKoin()
            val viewModel: ParkingLotDetailViewModel = remember { koin.get<ParkingLotDetailViewModel>() }

            val parkingLotId = selectedParkingLotId
            if (parkingLotId != null) {
                ParkingLotDetailScreen(
                    viewModel = viewModel,
                    parkingLotId = parkingLotId,
                    onCreateVehicleClick = {
                        selectedParkingLotId = parkingLotId
                        navController.navigate(Screen.VehicleList.route)
                    },
                    onManageGateClick = {
                        selectedParkingLotId = parkingLotId
                        navController.navigate(Screen.GateList.route)
                    },
                    onPlateDetectionClick = {
                        selectedParkingLotId = parkingLotId
                        navController.navigate(Screen.PlateDetection.route)
                    }
                )
            } else {
                Text("유효하지 않은 주차장 ID")
            }
        }

        composable(Screen.VehicleList.route) {
            val koin = getKoin()
            val viewModel: VehicleViewModel = remember { koin.get<VehicleViewModel>() }

            val lotId = selectedParkingLotId
            if (lotId == null) {
                Text("유효하지 않은 주차장 ID")
            } else {
                VehicleListScreen(
                    viewModel = viewModel,
                    parkingLotId = lotId,
                    onBack = { navController.popBackStack() },
                    onCreateClick = { navController.navigate(Screen.CreateVehicle.route) }
                )
            }
        }

        composable(Screen.CreateVehicle.route) {
            val koin = getKoin()
            val viewModel: VehicleViewModel = remember { koin.get<VehicleViewModel>() }

            val lotId = selectedParkingLotId
            if (lotId == null) {
                Text("유효하지 않은 주차장 ID")
            } else {
                CreateVehicleScreen(
                    viewModel = viewModel,
                    parkingLotId = lotId,
                    onCreated = {
                        navController.popBackStack() // CreateVehicle 닫기
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable(Screen.GateList.route) {
            val koin = getKoin()
            val viewModel: GateViewModel = remember { koin.get<GateViewModel>() }
            val lotId = selectedParkingLotId

            if (lotId != null) {
                GateListScreen(
                    viewModel = viewModel,
                    parkingLotId = lotId,
                    onBack = { navController.popBackStack() },
                    onCreateClick = { navController.navigate(Screen.CreateGate.route) }
                )
            } else {
                Text("유효하지 않은 주차장 ID")
            }
        }

        composable(Screen.CreateGate.route) {
            val koin = getKoin()
            val viewModel: GateViewModel = remember { koin.get<GateViewModel>() }
            val lotId = selectedParkingLotId

            if (lotId != null) {
                CreateGateScreen(
                    viewModel = viewModel,
                    parkingLotId = lotId,
                    onCreated = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            } else {
                Text("유효하지 않은 주차장 ID")
            }
        }

        composable(Screen.PlateDetection.route) {
            val koin = getKoin()
            val viewModel: PlateDetectionViewModel = remember { koin.get<PlateDetectionViewModel>() }
            val lotId = selectedParkingLotId

            if (lotId != null) {
                PlateDetectionScreen(
                    viewModel = viewModel,
                    parkingLotId = lotId,
                    onBack = { navController.popBackStack() }
                )
            } else {
                Text("유효하지 않은 주차장 ID")
            }
        }
    }
}