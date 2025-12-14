package com.sangyoon.parkingpass.di

import androidx.lifecycle.viewmodel.compose.viewModel
import com.sangyoon.parkingpass.api.ParkingApiClient
import com.sangyoon.parkingpass.data.datasource.ParkingApiDataSource
import com.sangyoon.parkingpass.data.repository.ParkingLotRepositoryImpl
import com.sangyoon.parkingpass.domain.repository.ParkingLotRepository
import com.sangyoon.parkingpass.domain.usecase.CreateParkingLotUseCase
import com.sangyoon.parkingpass.domain.usecase.GetParkingLotUseCase
import com.sangyoon.parkingpass.domain.usecase.GetParkingLotsUseCase
import com.sangyoon.parkingpass.presentation.viewmodel.ParkingLotViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val appModule = module {
    // API Client
    single<ParkingApiClient> {
        ParkingApiClient("http://10.0.2.2:8080")    // Android Emulator
    }

    // Data Source
    singleOf(::ParkingApiDataSource)

    // Repository
    single<ParkingLotRepository> { ParkingLotRepositoryImpl(get()) }

    // Use Case
    factoryOf(::GetParkingLotsUseCase)
    factoryOf(::GetParkingLotUseCase)
    factoryOf(::CreateParkingLotUseCase)

    // ViewModel
    factory { ParkingLotViewModel(get(), get()) }
}