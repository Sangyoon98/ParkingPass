package com.sangyoon.parkingpass.presentation.navigation

import androidx.compose.runtime.Composable

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    // iOS는 뒤로가기 버튼이 없으므로 구현 불필요
}

