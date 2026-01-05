package com.sangyoon.parkingpass.di

import com.sangyoon.parkingpass.api.ParkingApiClient
import com.sangyoon.parkingpass.data.datasource.ParkingApiDataSource
import com.sangyoon.parkingpass.data.repository.GateRepositoryImpl
import com.sangyoon.parkingpass.data.repository.ParkingLotRepositoryImpl
import com.sangyoon.parkingpass.data.repository.PlateDetectionRepositoryImpl
import com.sangyoon.parkingpass.data.repository.SessionRepositoryImpl
import com.sangyoon.parkingpass.data.repository.VehicleRepositoryImpl
import com.sangyoon.parkingpass.data.repository.AuthRepository
import com.sangyoon.parkingpass.data.repository.ParkingLotMemberRepositoryImpl
import com.sangyoon.parkingpass.data.storage.SecureStorage
import com.sangyoon.parkingpass.data.storage.createSecureStorage
import com.sangyoon.parkingpass.domain.repository.GateRepository
import com.sangyoon.parkingpass.domain.repository.ParkingLotMemberRepository
import com.sangyoon.parkingpass.domain.repository.ParkingLotRepository
import com.sangyoon.parkingpass.domain.repository.PlateDetectionRepository
import com.sangyoon.parkingpass.domain.repository.SessionRepository
import com.sangyoon.parkingpass.domain.repository.VehicleRepository
import com.sangyoon.parkingpass.domain.usecase.CreateParkingLotUseCase
import com.sangyoon.parkingpass.domain.usecase.CreateVehicleUseCase
import com.sangyoon.parkingpass.domain.usecase.GetMyParkingLotsUseCase
import com.sangyoon.parkingpass.domain.usecase.GetGatesUseCase
import com.sangyoon.parkingpass.domain.usecase.GetOpenSessionsUseCase
import com.sangyoon.parkingpass.domain.usecase.GetParkingLotDetailUseCase
import com.sangyoon.parkingpass.domain.usecase.GetParkingLotsUseCase
import com.sangyoon.parkingpass.domain.usecase.GetParkingLotMembersUseCase
import com.sangyoon.parkingpass.domain.usecase.InviteParkingLotMemberUseCase
import com.sangyoon.parkingpass.domain.usecase.GetCurrentSessionByPlateUseCase
import com.sangyoon.parkingpass.domain.usecase.GetSessionHistoryUseCase
import com.sangyoon.parkingpass.domain.usecase.GetVehicleByPlateUseCase
import com.sangyoon.parkingpass.domain.usecase.GetVehiclesUseCase
import com.sangyoon.parkingpass.domain.usecase.PlateDetectedUseCase
import com.sangyoon.parkingpass.domain.usecase.RegisterGateUseCase
import com.sangyoon.parkingpass.domain.usecase.SearchParkingLotsUseCase
import com.sangyoon.parkingpass.domain.usecase.RequestJoinParkingLotUseCase
import com.sangyoon.parkingpass.domain.usecase.ApproveParkingLotMemberUseCase
import com.sangyoon.parkingpass.domain.usecase.RejectParkingLotMemberUseCase
import com.sangyoon.parkingpass.domain.usecase.UpdateMemberRoleUseCase
import com.sangyoon.parkingpass.domain.usecase.RemoveParkingLotMemberUseCase
import com.sangyoon.parkingpass.presentation.viewmodel.GateViewModel
import com.sangyoon.parkingpass.presentation.viewmodel.ParkingLotDetailViewModel
import com.sangyoon.parkingpass.presentation.viewmodel.ParkingLotViewModel
import com.sangyoon.parkingpass.presentation.viewmodel.ParkingLotSearchViewModel
import com.sangyoon.parkingpass.presentation.viewmodel.ParkingLotMemberViewModel
import com.sangyoon.parkingpass.presentation.viewmodel.PlateDetectionViewModel
import com.sangyoon.parkingpass.presentation.viewmodel.SessionViewModel
import com.sangyoon.parkingpass.presentation.viewmodel.VehicleViewModel
import com.sangyoon.parkingpass.presentation.viewmodel.AuthViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val appModule = module {
    // API Client - EC2 서버 주소
    single<ParkingApiClient> {
        ParkingApiClient("http://13.124.55.217:8080")
    }

    // Data Source
    singleOf(::ParkingApiDataSource)

    // Repository
    single<ParkingLotRepository> { ParkingLotRepositoryImpl(get()) }
    single<SessionRepository> { SessionRepositoryImpl(get()) }
    single<VehicleRepository> { VehicleRepositoryImpl(get()) }
    single<GateRepository> { GateRepositoryImpl(get()) }
    single<PlateDetectionRepository> { PlateDetectionRepositoryImpl(get()) }
    single<ParkingLotMemberRepository> { ParkingLotMemberRepositoryImpl(get()) }
    single<SecureStorage> { createSecureStorage() }
    single { AuthRepository(get(), get()) }

    // Use Case
    factoryOf(::GetParkingLotsUseCase)
    factoryOf(::GetMyParkingLotsUseCase)
    factoryOf(::GetParkingLotDetailUseCase)
    factoryOf(::CreateParkingLotUseCase)
    factoryOf(::GetOpenSessionsUseCase)
    factoryOf(::GetSessionHistoryUseCase)
    factoryOf(::GetVehiclesUseCase)
    factoryOf(::CreateVehicleUseCase)
    factoryOf(::GetGatesUseCase)
    factoryOf(::RegisterGateUseCase)
    factoryOf(::PlateDetectedUseCase)
    factoryOf(::GetVehicleByPlateUseCase)
    factoryOf(::GetCurrentSessionByPlateUseCase)
    factoryOf(::SearchParkingLotsUseCase)
    factoryOf(::RequestJoinParkingLotUseCase)
    factoryOf(::GetParkingLotMembersUseCase)
    factoryOf(::InviteParkingLotMemberUseCase)
    factoryOf(::ApproveParkingLotMemberUseCase)
    factoryOf(::RejectParkingLotMemberUseCase)
    factoryOf(::UpdateMemberRoleUseCase)
    factoryOf(::RemoveParkingLotMemberUseCase)

    // ViewModel
    factory { ParkingLotViewModel(get(), get(), get()) }
    factory { ParkingLotDetailViewModel(get()) }
    factory { VehicleViewModel(get(), get()) }
    factory { GateViewModel(get(), get()) }
    factory { PlateDetectionViewModel(get(), get(), get(), get()) }
    factory { SessionViewModel(get(), get()) }
    factory { AuthViewModel(get()) }
    factory { ParkingLotSearchViewModel(get(), get()) }
    factory { ParkingLotMemberViewModel(get(), get(), get(), get(), get(), get()) }
}
