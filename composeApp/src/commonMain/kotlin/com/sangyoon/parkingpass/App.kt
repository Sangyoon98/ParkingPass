package com.sangyoon.parkingpass

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.sangyoon.parkingpass.di.appModule
import com.sangyoon.parkingpass.presentation.navigation.ParkingAppNavigation
import com.sangyoon.parkingpass.presentation.ui.LoginScreen
import com.sangyoon.parkingpass.presentation.viewmodel.AuthViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Preview
fun App() {
    KoinApplication(application = {
        modules(appModule)
    }) {
        val authViewModel = koinViewModel<AuthViewModel>()
        val authState by authViewModel.uiState.collectAsState()

        MaterialTheme {
            if (authState.currentUser == null) {
                LoginScreen(viewModel = authViewModel)
            } else {
                ParkingAppNavigation()
            }
        }
    }
}
