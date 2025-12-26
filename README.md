This is a Kotlin Multiplatform project targeting Android, iOS, Server.

* [/composeApp](./composeApp/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - [commonMain](./composeApp/src/commonMain/kotlin) is for code that's common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple's CoreCrypto for the iOS part of your Kotlin app,
    the [iosMain](./composeApp/src/iosMain/kotlin) folder would be the right place for such calls.
    Similarly, if you want to edit the Desktop (JVM) specific part, the [jvmMain](./composeApp/src/jvmMain/kotlin)
    folder is the appropriate location.

* [/iosApp](./iosApp/iosApp) contains iOS applications. Even if you're sharing your UI with Compose Multiplatform,
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.

* [/server](./server/src/main/kotlin) is for the Ktor server application.

* [/shared](./shared/src) is for the code that will be shared between all targets in the project.
  The most important subfolder is [commonMain](./shared/src/commonMain/kotlin). If preferred, you
  can add code to the platform-specific folders here too.

### Build and Run Android Application

To build and run the development version of the Android app, use the run configuration from the run widget
in your IDE's toolbar or build it directly from the terminal:
- on macOS/Linux
  ```shell
  ./gradlew :composeApp:assembleDebug
  ```
- on Windows
  ```shell
  .\gradlew.bat :composeApp:assembleDebug
  ```

### Build and Run Server

#### Local Development

To build and run the development version of the server, use the run configuration from the run widget
in your IDE's toolbar or run it directly from the terminal:
- on macOS/Linux
  ```shell
  ./gradlew :server:run
  ```
- on Windows
  ```shell
  .\gradlew.bat :server:run
  ```

#### Docker

Build and run with Docker:

1. **Build the image:**
   ```shell
   docker build -t parkingpass-server:latest .
   ```

2. **Run with environment variables:**
   ```shell
   docker run -e SUPABASE_URL="https://your-project.supabase.co" \
              -e SUPABASE_SERVICE_ROLE_KEY="your_service_role_key" \
              -p 8080:8080 \
              parkingpass-server:latest
   ```

3. **Or use docker-compose:**
   
   Create a `.env` file with your Supabase credentials:
   ```shell
   cp .env.example .env
   # Edit .env and set SUPABASE_URL and SUPABASE_SERVICE_ROLE_KEY
   docker-compose up -d
   ```
   
   The `.env` file format:
   ```
   SUPABASE_URL=https://your-project.supabase.co
   SUPABASE_SERVICE_ROLE_KEY=your_service_role_key
   ```

#### Deployment to AWS EC2

See [DEPLOY.md](./DEPLOY.md) for detailed deployment instructions to AWS EC2.

Quick deployment steps:
1. Build Docker image: `docker build --platform linux/amd64 -t parkingpass-server:latest .`
2. Push to ECR: Tag and push to your ECR repository
3. On EC2: Pull image and run with `docker-compose` or `docker run`

#### CI/CD with GitHub Actions

See [docs/GITHUB_ACTIONS_SETUP.md](./docs/GITHUB_ACTIONS_SETUP.md) for GitHub Actions CI/CD setup.

The repository includes:
- **CI Workflow**: Automated testing on pull requests
- **Deploy Workflow**: Automatic deployment to EC2 on push to `main` branch

### Build and Run iOS Application

To build and run the development version of the iOS app, use the run configuration from the run widget
in your IDE's toolbar or open the [/iosApp](./iosApp) directory in Xcode and run it from there.

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…
