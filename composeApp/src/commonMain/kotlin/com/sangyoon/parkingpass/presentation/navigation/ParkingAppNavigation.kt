package com.sangyoon.parkingpass.presentation.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sangyoon.parkingpass.presentation.ui.CreateParkingLotScreen
import com.sangyoon.parkingpass.presentation.ui.ParkingLotDetailScreen
import com.sangyoon.parkingpass.presentation.ui.ParkingLotListScreen
import com.sangyoon.parkingpass.presentation.viewmodel.ParkingLotDetailViewModel
import com.sangyoon.parkingpass.presentation.viewmodel.ParkingLotViewModel
import org.koin.compose.getKoin

sealed class Screen(val route: String) {
    object ParkingLotList : Screen("parking_lot_list")
    object ParkingLotDetail : Screen("parking_lot_detail")
    object CreateParkingLot : Screen("create_parking_lot")
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
            val viewModel: ParkingLotViewModel = getKoin().get<ParkingLotViewModel>()
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
            val vm: ParkingLotViewModel = getKoin().get()
            CreateParkingLotScreen(
                viewModel = vm,
                onCreated = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ParkingLotDetail.route) {
            val vm: ParkingLotDetailViewModel = getKoin().get()

            val parkingLotId = selectedParkingLotId
            if (parkingLotId != null) {
                ParkingLotDetailScreen(
                    viewModel = vm,
                    parkingLotId = parkingLotId,
                    onCreateVehicleClick = { /* TODO */ },
                    onManageGateClick = { /* TODO */ }
                )
            } else {
                Text("유효하지 않은 주차장 ID")
            }
        }
    }
}