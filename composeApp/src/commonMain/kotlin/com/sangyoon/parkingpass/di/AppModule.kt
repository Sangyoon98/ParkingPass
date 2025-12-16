package com.sangyoon.parkingpass.di

import com.sangyoon.parkingpass.api.ParkingApiClient
import com.sangyoon.parkingpass.data.datasource.ParkingApiDataSource
import com.sangyoon.parkingpass.data.repository.GateRepositoryImpl
import com.sangyoon.parkingpass.data.repository.ParkingLotRepositoryImpl
import com.sangyoon.parkingpass.data.repository.SessionRepositoryImpl
import com.sangyoon.parkingpass.data.repository.VehicleRepositoryImpl
import com.sangyoon.parkingpass.domain.repository.GateRepository
import com.sangyoon.parkingpass.domain.repository.ParkingLotRepository
import com.sangyoon.parkingpass.domain.repository.SessionRepository
import com.sangyoon.parkingpass.domain.repository.VehicleRepository
import com.sangyoon.parkingpass.domain.usecase.CreateParkingLotUseCase
import com.sangyoon.parkingpass.domain.usecase.CreateVehicleUseCase
import com.sangyoon.parkingpass.domain.usecase.GetGatesUseCase
import com.sangyoon.parkingpass.domain.usecase.GetOpenSessionsUseCase
import com.sangyoon.parkingpass.domain.usecase.GetParkingLotDetailUseCase
import com.sangyoon.parkingpass.domain.usecase.GetParkingLotsUseCase
import com.sangyoon.parkingpass.domain.usecase.GetSessionHistoryUseCase
import com.sangyoon.parkingpass.domain.usecase.GetVehiclesUseCase
import com.sangyoon.parkingpass.domain.usecase.RegisterGateUseCase
import com.sangyoon.parkingpass.presentation.viewmodel.GateViewModel
import com.sangyoon.parkingpass.presentation.viewmodel.ParkingLotDetailViewModel
import com.sangyoon.parkingpass.presentation.viewmodel.ParkingLotViewModel
import com.sangyoon.parkingpass.presentation.viewmodel.VehicleViewModel
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
    single<SessionRepository> { SessionRepositoryImpl(get()) }
    single<VehicleRepository> { VehicleRepositoryImpl(get()) }
    single<GateRepository> { GateRepositoryImpl(get()) }

    // Use Case
    factoryOf(::GetParkingLotsUseCase)
    factoryOf(::GetParkingLotDetailUseCase)
    factoryOf(::CreateParkingLotUseCase)
    factoryOf(::GetParkingLotDetailUseCase)
    factoryOf(::GetOpenSessionsUseCase)
    factoryOf(::GetSessionHistoryUseCase)
    factoryOf(::GetVehiclesUseCase)
    factoryOf(::CreateVehicleUseCase)
    factoryOf(::GetGatesUseCase)
    factoryOf(::RegisterGateUseCase)

    // ViewModel
    factory { ParkingLotViewModel(get(), get()) }
    factory { ParkingLotDetailViewModel(get(), get(),get()) }
    factory { VehicleViewModel(get(), get()) }
    factory { GateViewModel(get(), get()) }
}