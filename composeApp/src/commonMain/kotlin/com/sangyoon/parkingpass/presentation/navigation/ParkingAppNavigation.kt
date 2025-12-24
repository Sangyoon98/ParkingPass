package com.sangyoon.parkingpass.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
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
import com.sangyoon.parkingpass.presentation.ui.SessionListScreen
import com.sangyoon.parkingpass.presentation.ui.VehicleListScreen
import com.sangyoon.parkingpass.presentation.viewmodel.GateViewModel
import com.sangyoon.parkingpass.presentation.viewmodel.ParkingLotDetailViewModel
import com.sangyoon.parkingpass.presentation.viewmodel.ParkingLotViewModel
import com.sangyoon.parkingpass.presentation.viewmodel.PlateDetectionViewModel
import com.sangyoon.parkingpass.presentation.viewmodel.SessionViewModel
import com.sangyoon.parkingpass.presentation.viewmodel.VehicleViewModel
import org.koin.compose.viewmodel.koinViewModel

object Routes {
    const val PARKING_LOT_LIST = "parking_lot_list"
    const val CREATE_PARKING_LOT = "create_parking_lot"
    const val PARKING_LOT_DETAIL = "parking_lot_detail"
    const val VEHICLE_LIST = "vehicle_list"
    const val CREATE_VEHICLE = "create_vehicle"
    const val GATE_LIST = "gate_list"
    const val CREATE_GATE = "create_gate"
    const val PLATE_DETECTION = "plate_detection"
    const val SESSION_LIST = "session_list"
}

@Composable
fun ParkingAppNavigation(
    navController: NavHostController = rememberNavController(),
    navigationState: NavigationState = rememberNavigationState()
) {
    CompositionLocalProvider(LocalNavigationState provides navigationState) {
        NavHost(
            navController = navController,
            startDestination = Routes.PARKING_LOT_LIST
        ) {
            composable(Routes.PARKING_LOT_LIST) {
                val viewModel = koinViewModel<ParkingLotViewModel>()
                ParkingLotListScreen(
                    viewModel = viewModel,
                    onParkingLotClick = { parkingLotId ->
                        navigationState.setSelectedParkingLotId(parkingLotId)
                        navController.navigate(Routes.PARKING_LOT_DETAIL)
                    },
                    onCreateClick = {
                        navController.navigate(Routes.CREATE_PARKING_LOT)
                    }
                )
            }

            composable(Routes.CREATE_PARKING_LOT) {
                val viewModel = koinViewModel<ParkingLotViewModel>()
                CreateParkingLotScreen(
                    viewModel = viewModel,
                    onCreated = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.PARKING_LOT_DETAIL) {
                val viewModel = koinViewModel<ParkingLotDetailViewModel>()
                val parkingLotId = navigationState.selectedParkingLotId
                
                if (parkingLotId != null) {
                    LaunchedEffect(parkingLotId) {
                        viewModel.setSelectedParkingLotId(parkingLotId)
                    }
                    
                    ParkingLotDetailScreen(
                        viewModel = viewModel,
                        parkingLotId = parkingLotId,
                        onCreateVehicleClick = {
                            navController.navigate(Routes.VEHICLE_LIST)
                        },
                        onManageGateClick = {
                            navController.navigate(Routes.GATE_LIST)
                        },
                        onPlateDetectionClick = {
                            navController.navigate(Routes.PLATE_DETECTION)
                        },
                        onSessionListClick = {
                            navController.navigate(Routes.SESSION_LIST)
                        },
                        onBack = { navController.popBackStack() }
                    )
                }
            }

            composable(Routes.VEHICLE_LIST) {
                val viewModel = koinViewModel<VehicleViewModel>()
                val parkingLotId = navigationState.selectedParkingLotId
                
                if (parkingLotId != null) {
                    LaunchedEffect(parkingLotId) {
                        viewModel.setSelectedParkingLotId(parkingLotId)
                    }
                    
                    VehicleListScreen(
                        viewModel = viewModel,
                        parkingLotId = parkingLotId,
                        onBack = { navController.popBackStack() },
                        onCreateClick = {
                            navController.navigate(Routes.CREATE_VEHICLE)
                        }
                    )
                }
            }

            composable(Routes.CREATE_VEHICLE) {
                val viewModel = koinViewModel<VehicleViewModel>()
                val parkingLotId = navigationState.selectedParkingLotId
                
                if (parkingLotId != null) {
                    LaunchedEffect(parkingLotId) {
                        viewModel.setSelectedParkingLotId(parkingLotId)
                    }
                    
                    CreateVehicleScreen(
                        viewModel = viewModel,
                        parkingLotId = parkingLotId,
                        onCreated = { navController.popBackStack() },
                        onBack = { navController.popBackStack() }
                    )
                }
            }

            composable(Routes.GATE_LIST) {
                val viewModel = koinViewModel<GateViewModel>()
                val parkingLotId = navigationState.selectedParkingLotId
                
                if (parkingLotId != null) {
                    LaunchedEffect(parkingLotId) {
                        viewModel.setSelectedParkingLotId(parkingLotId)
                    }
                    
                    GateListScreen(
                        viewModel = viewModel,
                        parkingLotId = parkingLotId,
                        onBack = { navController.popBackStack() },
                        onCreateClick = {
                            navController.navigate(Routes.CREATE_GATE)
                        }
                    )
                }
            }

            composable(Routes.CREATE_GATE) {
                val viewModel = koinViewModel<GateViewModel>()
                val parkingLotId = navigationState.selectedParkingLotId
                
                if (parkingLotId != null) {
                    LaunchedEffect(parkingLotId) {
                        viewModel.setSelectedParkingLotId(parkingLotId)
                    }
                    
                    CreateGateScreen(
                        viewModel = viewModel,
                        parkingLotId = parkingLotId,
                        onCreated = { navController.popBackStack() },
                        onBack = { navController.popBackStack() }
                    )
                }
            }

            composable(Routes.PLATE_DETECTION) {
                val viewModel = koinViewModel<PlateDetectionViewModel>()
                val parkingLotId = navigationState.selectedParkingLotId
                
                if (parkingLotId != null) {
                    LaunchedEffect(parkingLotId) {
                        viewModel.setSelectedParkingLotId(parkingLotId)
                    }
                    
                    PlateDetectionScreen(
                        viewModel = viewModel,
                        parkingLotId = parkingLotId,
                        onBack = { navController.popBackStack() }
                    )
                }
            }

            composable(Routes.SESSION_LIST) {
                val viewModel = koinViewModel<SessionViewModel>()
                val parkingLotId = navigationState.selectedParkingLotId
                
                if (parkingLotId != null) {
                    LaunchedEffect(parkingLotId) {
                        viewModel.setSelectedParkingLotId(parkingLotId)
                    }
                    
                    SessionListScreen(
                        viewModel = viewModel,
                        parkingLotId = parkingLotId,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
