import io.ktor.plugin.OpenApiPreview
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    application
    kotlin("plugin.serialization") version libs.versions.kotlin.get()
    id("com.github.johnrengelman.shadow")
}

// local.properties에서 Supabase 설정 읽기
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
var supabaseUrl: String? = null
var supabaseKey: String? = null

if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
    supabaseUrl = localProperties.getProperty("SUPABASE_URL")
    supabaseKey = localProperties.getProperty("SUPABASE_SERVICE_ROLE_KEY")
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

// OpenAPI 파일 후처리: path parameter의 parkingLotId와 id를 integer로 수정
tasks.register("fixOpenApiTypes") {
    dependsOn("buildOpenApi")
    doLast {
        val openApiFile = file("${layout.buildDirectory.get()}/openapi/generated.json")
        if (openApiFile.exists()) {
            var content = openApiFile.readText()
            
            // path parameter의 id를 integer로 수정
            content = content.replace(
                """                        "schema": {
                            "type": "string"
                        }
                    }
                ],
                "parameters": [
                    {
                        "name": "id",
                        "in": "path",
                        "description": "주차장 ID",
                        "required": true,
                        "schema": {
                            "type": "string"
                        }""",
                """                        "schema": {
                            "type": "string"
                        }
                    }
                ],
                "parameters": [
                    {
                        "name": "id",
                        "in": "path",
                        "description": "주차장 ID",
                        "required": true,
                        "schema": {
                            "type": "integer",
                            "format": "int64"
                        }"""
            )
            
            // path parameter의 parkingLotId를 integer로 수정 (모든 발생)
            content = content.replace(
                """                        "name": "parkingLotId",
                        "in": "path",
                        "required": true,
                        "schema": {
                            "type": "string"
                        }""",
                """                        "name": "parkingLotId",
                        "in": "path",
                        "required": true,
                        "schema": {
                            "type": "integer",
                            "format": "int64"
                        }"""
            )
            
            openApiFile.writeText(content)
            println("✅ OpenAPI path parameter types fixed (id, parkingLotId -> integer)")
        }
    }
}

// OpenAPI 파일을 리소스로 복사하는 태스크
tasks.register<Copy>("copyOpenApiToResources") {
    dependsOn("fixOpenApiTypes")
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

// ShadowJar 설정 (Fat JAR 생성)
tasks.shadowJar {
    archiveBaseName.set("server")
    archiveClassifier.set("")
    archiveVersion.set("")
    manifest {
        attributes["Main-Class"] = "com.sangyoon.parkingpass.ApplicationKt"
    }
    // 중복 파일 처리 전략
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

application {
    mainClass.set("com.sangyoon.parkingpass.ApplicationKt")
    
    val isDevelopment: Boolean = project.ext.has("development")
    val jvmArgs = mutableListOf("-Dio.ktor.development=$isDevelopment")
    
    // local.properties에서 Supabase 설정을 JVM 인자로 추가
    if (supabaseUrl != null) {
        jvmArgs.add("-DSUPABASE_URL=$supabaseUrl")
    }
    if (supabaseKey != null) {
        jvmArgs.add("-DSUPABASE_SERVICE_ROLE_KEY=$supabaseKey")
    }
    
    applicationDefaultJvmArgs = jvmArgs
}

tasks.named<JavaExec>("run") {
    val isDevelopment: Boolean = project.ext.has("development")
    val jvmArgs = mutableListOf("-Dio.ktor.development=$isDevelopment")
    
    if (supabaseUrl != null) {
        jvmArgs.add("-DSUPABASE_URL=$supabaseUrl")
    }
    if (supabaseKey != null) {
        jvmArgs.add("-DSUPABASE_SERVICE_ROLE_KEY=$supabaseKey")
    }
    
    jvmArgs(jvmArgs)
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

    // Supabase
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.postgrest)
    implementation(libs.ktor.client.cio) // HTTP client for Supabase

    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotlin.testJunit)
}