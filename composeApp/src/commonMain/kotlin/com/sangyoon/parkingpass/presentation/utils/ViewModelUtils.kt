package com.sangyoon.parkingpass.presentation.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import org.koin.compose.getKoin
import org.koin.core.Koin
import kotlin.reflect.KClass

@Composable
inline fun <reified VM : ViewModel> koinViewModelWithOwner(
    owner: ViewModelStoreOwner
): VM {
    val koin: Koin = getKoin()

    val factory = remember {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: KClass<T>,
                extras: CreationExtras
            ): T {
                // 여기서 Koin으로 직접 생성
                return when (modelClass) {
                    VM::class -> koin.get<VM>() as T
                    else -> throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
                }
            }
        }
    }

    return viewModel(
        viewModelStoreOwner = owner,
        factory = factory
    )
}