package com.sangyoon.parkingpass.presentation.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import org.koin.compose.getKoin

/**
 * Koin에서 ViewModel을 가져오는 Helper 함수
 * Compose 수명주기와 연동되어 ViewModel 인스턴스를 관리합니다.
 */
@Composable
inline fun <reified T : ViewModel> koinViewModel(): T {
    val koin = getKoin()
    return remember { koin.get<T>() }
}