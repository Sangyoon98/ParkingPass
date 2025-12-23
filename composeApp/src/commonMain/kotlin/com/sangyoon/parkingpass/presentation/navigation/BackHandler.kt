package com.sangyoon.parkingpass.presentation.navigation

import androidx.compose.runtime.Composable

@Composable
expect fun BackHandler(enabled: Boolean, onBack: () -> Unit)

