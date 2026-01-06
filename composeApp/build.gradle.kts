import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

// Keystore.properties
val keystorePropertiesFile: File = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
keystoreProperties.load(FileInputStream(keystorePropertiesFile))

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
        
        iosTarget.compilations.getByName("main") {
            cinterops {
                val CameraHelper by creating {
                    defFile(project.file("nativeInterop/cinterop/CameraHelper.def"))
                    packageName("com.sangyoon.parkingpass.camera.ios")
                    includeDirs(
                        project.file("nativeInterop/cinterop")
                    )
                }
                val KakaoLoginBridge by creating {
                    defFile(rootProject.file("nativeInterop/cinterop/KakaoLoginBridge.def"))
                    packageName("com.sangyoon.parkingpass.auth.bridge")
                    includeDirs(
                        project.file("nativeInterop/cinterop")
                    )
                }
                val SecureStorageBridge by creating {
                    defFile(rootProject.file("nativeInterop/cinterop/SecureStorageBridge.def"))
                    packageName("com.sangyoon.parkingpass.security.bridge")
                    includeDirs(
                        project.file("nativeInterop/cinterop")
                    )
                }
            }
        }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            
            // ML Kit Text Recognition (한국어)
            implementation(libs.mlkit.text.recognition.korean)
            
            // CameraX
            implementation(libs.cameraX.camera2)
            implementation(libs.cameraX.lifecycle)
            implementation(libs.cameraX.view)

            implementation(libs.v2.user)
            implementation(libs.androidx.security.crypto)
        }
        commonMain.dependencies {
            // Koin
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            // Navigation Compose
            implementation(libs.navigation.compose)

            implementation(libs.kotlinx.coroutines.core)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(projects.shared)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.sangyoon.parkingpass"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    val localProperties = Properties().apply {
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use { load(it) }
        }
    }
    val kakaoNativeKey = localProperties.getProperty("kakao.native.app.key")
        ?: localProperties.getProperty("KAKAO_NATIVE_APP_KEY")
        ?: System.getenv("KAKAO_NATIVE_APP_KEY")
        ?: ""

    val kakaoScheme = if (kakaoNativeKey.isNotEmpty()) {
        "kakao$kakaoNativeKey"
    } else {
        logger.warn("⚠️ kakao.native.app.key / KAKAO_NATIVE_APP_KEY is not configured. Kakao login will not work.")
        logger.warn("   Add 'kakao.native.app.key=YOUR_KEY' (or 'KAKAO_NATIVE_APP_KEY=YOUR_KEY') to local.properties or set KAKAO_NATIVE_APP_KEY in the environment.")
        "kakao-placeholder"
    }

    signingConfigs {
        create("release") {
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
        }
    }

    defaultConfig {
        applicationId = "com.sangyoon.parkingpass"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"

        resValue("string", "kakao_native_app_key", kakaoNativeKey)
        manifestPlaceholders["kakaoScheme"] = kakaoScheme
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}
