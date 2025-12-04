import io.ktor.plugin.OpenApiPreview

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    application
    kotlin("plugin.serialization") version "2.2.20"
}

group = "com.sangyoon.parkingpass"
version = "1.0.0"

ktor {
    @OptIn(OpenApiPreview::class)
    openApi {
        title = "Parking Pass API"
        version = "1.0.0"
        summary = "주차장 입출차 관리 API"
        description = "번호판 인식 기반 주차장 입출차 관리 시스템"
        license = "Apache/2.0"
        target = project.layout.buildDirectory.file("openapi/generated.json")
    }
}

// OpenAPI 파일을 리소스로 복사하는 태스크
tasks.register<Copy>("copyOpenApiToResources") {
    dependsOn("buildOpenApi")
    from("${layout.buildDirectory.get()}/openapi/generated.json")
    into("${sourceSets.main.get().resources.srcDirs.first()}/openapi")
}

// 빌드 시 자동으로 복사
tasks.named("processResources") {
    dependsOn("copyOpenApiToResources")
}

// build 태스크 실행 시 자동으로 buildOpenApi 실행
tasks.named("build") {
    dependsOn("buildOpenApi")
}

application {
    mainClass.set("com.sangyoon.parkingpass.ApplicationKt")
    
    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {
    // Swagger / OpenAPI
    implementation(libs.ktor.server.swagger)
    implementation(libs.ktor.server.openapi)

    // StatusPages
    implementation(libs.ktor.server.status.pages)

    implementation(projects.shared)
    implementation(libs.logback)
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)

    implementation(libs.ktor.server.content.negotiation.jvm)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.serialization.kotlinx.json.jvm)

    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotlin.testJunit)
}