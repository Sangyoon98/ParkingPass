package com.sangyoon.parkingpass.presentation.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sangyoon.parkingpass.presentation.ui.ParkingLotListScreen
import com.sangyoon.parkingpass.presentation.viewmodel.ParkingLotViewModel
import org.koin.compose.getKoin

sealed class Screen(val route: String) {
    object ParkingLotList : Screen("parking_lot_list")
    object ParkingLotDetail : Screen("parking_lot_detail/{parkingLotId}") {
        fun createRoute(parkingLotId: Long) = "parking_lot_detail/$parkingLotId"
    }
    object CreateParkingLot : Screen("create_parking_lot")
}

@Composable
fun ParkingAppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.ParkingLotList.route
    ) {
        composable(Screen.ParkingLotList.route) {
            val viewModel: ParkingLotViewModel = getKoin().get<ParkingLotViewModel>()
            ParkingLotListScreen(
                viewModel = viewModel,
                onParkingLotClick = { parkingLotId ->
                    navController.navigate(Screen.ParkingLotDetail.createRoute(parkingLotId))
                },
                onCreateClick = {
                    navController.navigate(Screen.CreateParkingLot.route)
                }
            )
        }

        composable(Screen.CreateParkingLot.route) {
            // TODO: CreateParkingLotScreen 구현 예정
            Text("주차장 생성 화면 (구현 예정)")
        }

        composable(
            route = Screen.ParkingLotDetail.route,
            arguments = listOf(
                navArgument("parkingLotId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            // route에서 직접 파싱하는 방법 (가장 안전)
            val parkingLotId = remember(backStackEntry) {
                val route = backStackEntry.destination.route ?: ""
                route.substringAfterLast("/").toLongOrNull() ?: 0L
            }
            Text("주차장 상세 화면 (ID: $parkingLotId) - 구현 예정")
        }
    }
}