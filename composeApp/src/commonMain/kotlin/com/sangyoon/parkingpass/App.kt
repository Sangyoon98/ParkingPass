package com.sangyoon.parkingpass

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.sangyoon.parkingpass.di.appModule
import com.sangyoon.parkingpass.presentation.navigation.ParkingAppNavigation
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication

@Composable
@Preview
fun App() {
    KoinApplication(application = {
        modules(appModule)
    }) {
        MaterialTheme {
            ParkingAppNavigation()
        }
    }
}