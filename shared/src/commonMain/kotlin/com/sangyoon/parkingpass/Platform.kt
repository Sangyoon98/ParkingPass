package com.sangyoon.parkingpass

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform