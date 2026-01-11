# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build and Run Commands

### Android App
```shell
./gradlew :composeApp:assembleDebug           # Build debug APK
./gradlew :composeApp:installDebug            # Build and install to device
```

### Ktor Server
```shell
./gradlew :server:run                         # Run locally (reads Supabase config from local.properties)
./gradlew :server:test                        # Run server tests
./gradlew :server:shadowJar                   # Build fat JAR for deployment
```

### Shared Module
```shell
./gradlew :shared:allTests                    # Run multiplatform unit tests
```

### Docker (Server)
```shell
docker build -t parkingpass-server:latest .   # Build image
docker-compose up -d                          # Run with .env credentials
```

### iOS
Open `iosApp/` in Xcode to build and run.

## Architecture Overview

This is a Kotlin Multiplatform project for parking lot management with license plate recognition.

### Module Structure
- **composeApp/**: Compose Multiplatform UI (Android/iOS). Uses Clean Architecture with Koin DI.
  - `commonMain`: Shared UI screens, ViewModels, domain models/use cases, repositories
  - `androidMain`: Android-specific implementations (ML Kit OCR, CameraX, Kakao SDK)
  - `iosMain`: iOS-specific implementations (Vision framework OCR, native camera bridge via cinterop)
- **server/**: Ktor API server with Netty, connects to Supabase for persistence.
  - Controllers define routes, Services contain business logic, Repositories handle data access
  - JWT authentication with Kakao OAuth support
  - OpenAPI spec auto-generated at build time (`copyOpenApiToResources` task)
- **shared/**: Multiplatform code shared across all targets (Android, iOS, JVM server).
  - `ParkingApiClient`: HTTP client for server communication
  - DTOs for API request/response types

### Key Architectural Patterns
- **Client (composeApp)**: Clean Architecture layers - presentation (ViewModels/UI states), domain (use cases/repositories interfaces), data (repository implementations, API data source)
- **Server**: Controller → Service → Repository pattern. Supabase repositories for production, in-memory implementations available for testing
- **DI**: Koin on client (`AppModule.kt`), manual DI on server (`Application.kt`)

## Configuration

### Required Environment/Properties
- `local.properties` (git-ignored):
  - `SUPABASE_URL` and `SUPABASE_SERVICE_ROLE_KEY` for server
  - `kakao.native.app.key` or `KAKAO_NATIVE_APP_KEY` for Android Kakao login
- `.env` for Docker deployment (same Supabase keys)
- `keystore.properties` for release signing (optional)

### API Base URL
Client connects to hardcoded EC2 server URL in `AppModule.kt`. Change `ParkingApiClient` base URL for different environments.

## Coding Conventions

- Follow Kotlin official style: 4-space indent, trailing commas, `UpperCamelCase` types, `lowerCamelCase` functions
- Compose screens use PascalCase filenames (e.g., `CameraOcrScreen.kt`)
- Server routes organized by feature in separate packages (e.g., `parkingpass.routes`)
- Prefer constructor injection; avoid singletons except in shared module
- Test naming: `<Function>Should<Expectation>`
- Commit messages: Conventional Commits format `<type>(scope): summary`
