package com.sangyoon.parkingpass

import com.sangyoon.parkingpass.auth.controller.authController
import com.sangyoon.parkingpass.auth.oauth.KakaoOAuthClient
import com.sangyoon.parkingpass.auth.repository.SupabaseUserRepository
import com.sangyoon.parkingpass.auth.service.AuthService
import com.sangyoon.parkingpass.config.SupabaseConfig
import com.sangyoon.parkingpass.gate.controller.gateController
import com.sangyoon.parkingpass.gate.service.GateService
import com.sangyoon.parkingpass.health.controller.healthController
import com.sangyoon.parkingpass.parking.repository.*
import com.sangyoon.parkingpass.parkingevent.controller.parkingEventController
import com.sangyoon.parkingpass.parkingevent.repository.*
import com.sangyoon.parkingpass.parkingevent.sevice.ParkingEventService
import com.sangyoon.parkingpass.parkinglot.controller.parkingLotController
import com.sangyoon.parkingpass.parkinglot.controller.parkingLotMemberController
import com.sangyoon.parkingpass.parkinglot.service.ParkingLotService
import com.sangyoon.parkingpass.parkinglot.service.ParkingLotMemberService
import com.sangyoon.parkingpass.session.controller.sessionController
import com.sangyoon.parkingpass.session.service.SessionService
import com.sangyoon.parkingpass.vehicle.controller.vehicleController
import com.sangyoon.parkingpass.vehicle.service.VehicleService
import com.sangyoon.parkingpass.common.AuthMiddleware
import com.sangyoon.parkingpass.common.configureStatusPages
import com.sangyoon.parkingpass.config.JwtConfig
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.openapi.openAPI
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.routing.*

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    // Supabase 클라이언트 초기화 (local.properties에서 설정 읽음)
    // local.properties에 값이 없으면 SupabaseConfig.createClient()에서 에러 발생
    val supabase = SupabaseConfig.createClient()

    val userRepository = SupabaseUserRepository(supabase)
    val kakaoOAuthClient = KakaoOAuthClient()
    environment.monitor.subscribe(ApplicationStopped) {
        kakaoOAuthClient.close()
    }
    val authService = AuthService(userRepository, kakaoOAuthClient)
    
    val parkingLotRepository: ParkingLotRepository = SupabaseParkingLotRepository(supabase)
    val parkingLotMemberRepository: ParkingLotMemberRepository = SupabaseParkingLotMemberRepository(supabase)
    val gateDeviceRepository: GateDeviceRepository = SupabaseGateDeviceRepository(supabase)
    val vehicleRepository: VehicleRepository = SupabaseVehicleRepository(supabase)
    val parkingSessionRepository: ParkingSessionRepository = SupabaseParkingSessionRepository(supabase)
    val parkingEventRepository: ParkingEventRepository = SupabaseParkingEventRepository(supabase)

    // Service 초기화
    val parkingLotService = ParkingLotService(
        parkingLotRepository = parkingLotRepository,
        parkingLotMemberRepository = parkingLotMemberRepository,
        userRepository = userRepository
    )
    val parkingLotMemberService = ParkingLotMemberService(
        parkingLotMemberRepository = parkingLotMemberRepository,
        parkingLotRepository = parkingLotRepository,
        userRepository = userRepository
    )
    val gateService = GateService(
        gateDeviceRepository = gateDeviceRepository,
        parkingLotRepository = parkingLotRepository
    )
    val vehicleService = VehicleService(
        vehicleRepository = vehicleRepository,
        parkingLotRepository = parkingLotRepository
    )
    val sessionService = SessionService(
        sessionRepository = parkingSessionRepository,
        vehicleRepository = vehicleRepository
    )
    val parkingEventService = ParkingEventService(
        eventRepository = parkingEventRepository,
        gateDeviceRepository = gateDeviceRepository,
        vehicleRepository = vehicleRepository,
        sessionRepository = parkingSessionRepository
    )
    val authMiddleware = AuthMiddleware(parkingLotMemberService)

    install(ContentNegotiation) { json() }
    configureStatusPages()
    install(Authentication) {
        jwt("auth-jwt") {
            realm = JwtConfig.realm
            verifier(JwtConfig.verifier)
            validate { credential ->
                val subject = credential.subject
                if (subject != null) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }

    routing {
        swaggerUI(path = "/swagger-ui", swaggerFile = "openapi/generated.json")
        openAPI("/docs", swaggerFile = "openapi/generated.json")

        healthController()
        authController(authService)
        parkingLotController(parkingLotService)
        parkingLotMemberController(parkingLotMemberService)
        parkingEventController(parkingEventService)
        vehicleController(vehicleService, authMiddleware)
        sessionController(sessionService, authMiddleware)
        gateController(gateService, authMiddleware)
    }
}
