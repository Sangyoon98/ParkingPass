package com.sangyoon.parkingpass.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sangyoon.parkingpass.presentation.ui.components.AppBottomNavigation

@Composable
fun MainScaffold(
    onNavigateToLogin: () -> Unit,
    onNavigateToParkingLotList: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        modifier = modifier,
        bottomBar = {
            AppBottomNavigation(
                currentRoute = currentRoute,
                onNavigate = { item ->
                    navController.navigate(item.route) {
                        // 백스택에서 시작 목적지까지 팝업
                        popUpTo(BottomNavItem.DASHBOARD.route) {
                            saveState = true
                        }
                        // 같은 아이템을 다시 선택했을 때 중복 방지
                        launchSingleTop = true
                        // 상태 복원
                        restoreState = true
                    }
                }
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.DASHBOARD.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            // 대시보드 탭 (나중에 구현)
            composable(BottomNavItem.DASHBOARD.route) {
                // TODO: DashboardScreen 연결
                // DashboardScreen()
            }

            // 차량 목록 탭 (기존 VehicleListScreen 사용)
            composable(BottomNavItem.VEHICLES.route) {
                // TODO: VehicleListScreen 연결
                // VehicleListScreen()
            }

            // 설정 탭 (나중에 구현)
            composable(BottomNavItem.SETTINGS.route) {
                // TODO: SettingsScreen 연결
                // SettingsScreen(
                //     onNavigateToParkingLotList = onNavigateToParkingLotList,
                //     onNavigateToLogin = onNavigateToLogin
                // )
            }
        }
    }
}
