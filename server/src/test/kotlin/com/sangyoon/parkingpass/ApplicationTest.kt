package com.sangyoon.parkingpass

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.*

class ApplicationTest {

    @Test
    fun testHealth() = testApplication {
        // 테스트 환경에서 Supabase 환경 변수 설정
        System.setProperty("SUPABASE_URL", "https://test.supabase.co")
        System.setProperty("SUPABASE_SERVICE_ROLE_KEY", "test-key")
        
        try {
            application {
                module()
            }
            val response = client.get("/api/v1/health")
            assertEquals(HttpStatusCode.OK, response.status)
            assertTrue(response.bodyAsText().startsWith("Ktor:"))
        } finally {
            // 테스트 후 정리
            System.clearProperty("SUPABASE_URL")
            System.clearProperty("SUPABASE_SERVICE_ROLE_KEY")
        }
    }
}