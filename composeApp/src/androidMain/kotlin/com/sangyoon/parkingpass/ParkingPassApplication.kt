package com.sangyoon.parkingpass

import android.app.Application

class ParkingPassApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        _instance = this
    }

    companion object {
        @Volatile
        private var _instance: ParkingPassApplication? = null

        val instance: ParkingPassApplication
            get() = _instance
                ?: throw IllegalStateException("ParkingPassApplication is not initialized yet.")

        fun isInitialized(): Boolean = _instance != null
    }
}
