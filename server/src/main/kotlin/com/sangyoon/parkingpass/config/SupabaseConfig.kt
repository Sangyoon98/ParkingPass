package com.sangyoon.parkingpass.config

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.ktor.client.engine.cio.CIO

object SupabaseConfig {
    private val supabaseUrl: String by lazy {
        // 1순위: 환경변수 (Docker/EC2에서 사용)
        // 2순위: JVM 프로퍼티 (로컬 실행 시)
        System.getenv("SUPABASE_URL")
            ?: System.getProperty("SUPABASE_URL")
            ?: throw IllegalStateException("SUPABASE_URL is not set. Set it as environment variable or JVM property.")
    }

    private val supabaseKey: String by lazy {
        System.getenv("SUPABASE_SERVICE_ROLE_KEY")
            ?: System.getProperty("SUPABASE_SERVICE_ROLE_KEY")
            ?: throw IllegalStateException("SUPABASE_SERVICE_ROLE_KEY is not set. Set it as environment variable or JVM property.")
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

