package com.sangyoon.parkingpass.config

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.ktor.client.engine.cio.CIO
import java.io.File
import java.util.Properties

object SupabaseConfig {
    private val supabaseUrl: String by lazy {
        System.getProperty("SUPABASE_URL") ?: loadFromLocalProperties("SUPABASE_URL")
            ?: throw IllegalStateException("SUPABASE_URL is not set. Set it in local.properties")
    }
    
    private val supabaseKey: String by lazy {
        System.getProperty("SUPABASE_SERVICE_ROLE_KEY") ?: loadFromLocalProperties("SUPABASE_SERVICE_ROLE_KEY")
            ?: throw IllegalStateException("SUPABASE_SERVICE_ROLE_KEY is not set. Set it in local.properties")
    }
    
    private fun loadFromLocalProperties(key: String): String? {
        // 프로젝트 루트의 local.properties 찾기
        var currentDir = File(System.getProperty("user.dir"))
        var localPropertiesFile: File? = null
        
        // 최대 5단계 상위 디렉토리까지 탐색
        repeat(5) {
            val candidate = File(currentDir, "local.properties")
            if (candidate.exists()) {
                localPropertiesFile = candidate
                return@repeat
            }
            currentDir = currentDir.parentFile ?: return@repeat
        }
        
        return localPropertiesFile?.takeIf { it.exists() }?.let { file ->
            val properties = Properties()
            file.inputStream().use { stream ->
                properties.load(stream)
            }
            properties.getProperty(key)
        }
    }

    fun createClient(): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = supabaseUrl,
            supabaseKey = supabaseKey
        ) {
            install(Postgrest)
            httpEngine = CIO.create()
        }
    }
}

