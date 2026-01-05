package com.sangyoon.parkingpass

import android.app.Application

class ParkingPassApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        @Volatile
        lateinit var instance: ParkingPassApplication
            private set
    }
}
