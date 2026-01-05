package com.sangyoon.parkingpass.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sangyoon.parkingpass.presentation.ui.CameraScreen
import com.sangyoon.parkingpass.presentation.ui.CreateGateScreen
import com.sangyoon.parkingpass.presentation.ui.CreateParkingLotScreen
import com.sangyoon.parkingpass.presentation.ui.CreateVehicleScreen
import com.sangyoon.parkingpass.presentation.ui.GateListScreen
import com.sangyoon.parkingpass.presentation.ui.ParkingLotDetailScreen
import com.sangyoon.parkingpass.presentation.ui.ParkingLotListScreen
import com.sangyoon.parkingpass.presentation.ui.ParkingLotSearchScreen
import com.sangyoon.parkingpass.presentation.ui.ParkingLotMemberScreen
import com.sangyoon.parkingpass.presentation.ui.PlateDetectionScreen
import com.sangyoon.parkingpass.presentation.ui.SessionListScreen
import com.sangyoon.parkingpass.presentation.ui.VehicleListScreen
import com.sangyoon.parkingpass.camera.CameraImage
import com.sangyoon.parkingpass.presentation.viewmodel.GateViewModel
import com.sangyoon.parkingpass.presentation.viewmodel.ParkingLotDetailViewModel
import com.sangyoon.parkingpass.presentation.viewmodel.ParkingLotViewModel
import com.sangyoon.parkingpass.presentation.viewmodel.ParkingLotSearchViewModel
import com.sangyoon.parkingpass.presentation.viewmodel.ParkingLotMemberViewModel
import com.sangyoon.parkingpass.presentation.viewmodel.PlateDetectionViewModel
import com.sangyoon.parkingpass.presentation.viewmodel.SessionViewModel
import com.sangyoon.parkingpass.presentation.viewmodel.VehicleViewModel
import org.koin.compose.viewmodel.koinViewModel
import com.sangyoon.parkingpass.presentation.utils.koinViewModelWithOwner

object Routes {
    const val PARKING_LOT_LIST = "parking_lot_list"
    const val CREATE_PARKING_LOT = "create_parking_lot"
    const val PARKING_LOT_DETAIL = "parking_lot_detail"
    const val PARKING_LOT_SEARCH = "parking_lot_search"
    const val PARKING_LOT_MEMBERS = "parking_lot_members"
    const val VEHICLE_LIST = "vehicle_list"
    const val CREATE_VEHICLE = "create_vehicle"
    const val GATE_LIST = "gate_list"
    const val CREATE_GATE = "create_gate"
    const val PLATE_DETECTION = "plate_detection"
    const val CAMERA = "camera"
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
                    },
                    onSearchClick = {
                        navController.navigate(Routes.PARKING_LOT_SEARCH)
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
                        onManageMembersClick = {
                            navController.navigate(Routes.PARKING_LOT_MEMBERS)
                        },
                        onBack = { navController.popBackStack() }
                    )
                }
            }
            composable(Routes.PARKING_LOT_MEMBERS) {
                val viewModel = koinViewModel<ParkingLotMemberViewModel>()
                val parkingLotId = navigationState.selectedParkingLotId

                if (parkingLotId != null) {
                    ParkingLotMemberScreen(
                        viewModel = viewModel,
                        parkingLotId = parkingLotId,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
            composable(Routes.PARKING_LOT_SEARCH) {
                val viewModel = koinViewModel<ParkingLotSearchViewModel>()
                ParkingLotSearchScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onParkingLotClick = { parkingLotId ->
                        navigationState.setSelectedParkingLotId(parkingLotId)
                        navController.navigate(Routes.PARKING_LOT_DETAIL)
                    }
                )
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
                        onBack = { navController.popBackStack() },
                        onCameraClick = {
                            navController.navigate(Routes.CAMERA)
                        }
                    )
                }
            }

            composable(Routes.CAMERA) {
                // PlateDetectionScreen과 같은 ViewModel 인스턴스를 사용하도록
                // NavBackStackEntry를 통해 부모 화면의 ViewModel을 가져옴
                val parentEntry = remember(navController.currentBackStackEntry) {
                    navController.getBackStackEntry(Routes.PLATE_DETECTION)
                }
                val viewModel = koinViewModelWithOwner<PlateDetectionViewModel>(parentEntry)
                val parkingLotId = navigationState.selectedParkingLotId
                
                if (parkingLotId != null) {
                    LaunchedEffect(parkingLotId) {
                        viewModel.setSelectedParkingLotId(parkingLotId)
                    }
                    
                    CameraScreen(
                        viewModel = viewModel,
                        parkingLotId = parkingLotId,
                        onBack = { navController.popBackStack() },
                        onImageCaptured = { image: CameraImage ->
                            // 번호판 인식 완료 - 화면 종료
                            // ViewModel의 plateNumber가 이미 recognizePlateFromImage에서 설정됨
                            // 먼저 화면을 닫고, 그 다음 자동으로 입출차 처리 시도 (게이트가 선택되어 있으면)
                            navController.popBackStack()
                            
                            // 화면이 닫힌 후 입출차 처리 시도
                            viewModel.tryAutoDetectPlate {
                                // 성공 시 추가 작업 없음 (이미 PlateDetectionScreen에 있음)
                            }
                        }
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
