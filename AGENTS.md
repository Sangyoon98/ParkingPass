# Repository Guidelines

ParkingPass is a Kotlin Multiplatform suite that ships Android (Compose), iOS (Swift/Compose), and a Ktor server that integrates with Supabase. Follow these concise standards to keep contributions predictable and production ready.

## Project Structure & Module Organization
- `composeApp/` hosts shared Compose UI: put platform-agnostic UI/state in `src/commonMain`, target-specific shims in `androidMain` and `iosMain`, and unit/UI tests in `commonTest`.
- `shared/` encapsulates domain logic, Supabase clients, and multiplatform utilities; mirror the `commonMain`/platformMain split and keep test doubles in `src/commonTest`.
- `server/` is the Netty-based API (`src/main/kotlin`) plus Ktor resources and OpenAPI artifacts, with integration/spec tests under `src/test/kotlin`.
- `iosApp/` contains the Swift entry point and configuration needed by Xcode. Supporting docs, deployment scripts, and Docker assets live in `docs/`, `DEPLOY.md`, `Dockerfile`, and `docker-compose.yml`.

## Build, Test, and Development Commands
- `./gradlew :composeApp:assembleDebug` builds the Android APK; add `installDebug` to deploy to a device.
- `./gradlew :server:run` boots the API locally with Supabase credentials pulled from `local.properties` JVM args.
- `./gradlew :shared:allTests` runs the multiplatform unit suite; `:server:test` executes Ktor TestHost specs.
- `docker build -t parkingpass-server:latest .` followed by `docker-compose up -d` mirrors production and loads `.env` Supabase keys.

## Coding Style & Naming Conventions
- Follow the official Kotlin style: 4-space indents, trailing commas enabled, `UpperCamelCase` types, `lowerCamelCase` functions/props, and `SCREAMING_SNAKE_CASE` constants.
- Compose screens/components live in `commonMain` and use PascalCase file names (`CameraOcrScreen.kt`). Keep server routes in packages by feature (e.g., `parkingpass.routes`), one public endpoint per file.
- Prefer dependency injection through constructor parameters; avoid singletons outside the shared module.

## Testing Guidelines
- Use `kotlin.test` assertions for multiplatform code and Ktor `TestApplication` for server routes. Place fixtures beside tests in `commonTest` or `server/src/test`.
- Name tests `<Function>Should<Expectation>` (e.g., `LicensePlateParserShouldHandleLeadingZeros`). Target new behavior on every PR with failing-first tests and keep Supabase calls mocked.

## Commit & Pull Request Guidelines
- Match the existing Conventional Commit style: `<type>(scope): summary` such as `fix(camera-ocr): adjust number parsing`. Use English unless a domain-specific Korean term is required.
- Every PR must describe user impact, list main Gradle tasks executed locally, and link an issue or TODO ID. Attach logs or screenshots when UI or API surfaces change.

## Security & Configuration Tips
- Never hardcode Supabase secrets; store them in `local.properties` (Gradle) or `.env` for Docker/EC2 and load them through the provided Gradle and compose scripts.
- Double-check that generated `build/openapi/generated.json` is copied via `copyOpenApiToResources` before shipping server changes, and avoid committing `.env`/`.DS_Store` files ignored in `.gitignore`.
