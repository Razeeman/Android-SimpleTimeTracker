package com.example.util.simpletimetracker

import android.app.Application
import com.example.util.simpletimetracker.di.AppComponent
import com.example.util.simpletimetracker.di.AppModule
import com.example.util.simpletimetracker.di.DaggerAppComponent

class TimeTrackerApp : Application() {

    override fun onCreate() {
        super.onCreate()

        appComponent = DaggerAppComponent.builder()
            .appModule(AppModule(this))
            .build()
    }

    companion object {
        var appComponent: AppComponent? = null
    }
}