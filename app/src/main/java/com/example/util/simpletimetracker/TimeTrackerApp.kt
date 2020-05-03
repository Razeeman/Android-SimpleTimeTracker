package com.example.util.simpletimetracker

import android.app.Application
import com.example.util.simpletimetracker.di.AppModule
import com.example.util.simpletimetracker.di.DaggerAppComponent
import com.example.util.simpletimetracker.di.FeatureComponentProvider
import com.example.util.simpletimetracker.feature_running_records.di.RunningRecordsComponent

class TimeTrackerApp : Application(), FeatureComponentProvider {

    var runningRecordsComponent: RunningRecordsComponent? = null

    override fun onCreate() {
        super.onCreate()

        val appComponent = DaggerAppComponent.builder()
            .appModule(AppModule(this))
            .build()
        runningRecordsComponent = appComponent
            ?.plusRunningRecordsComponent()
    }

    override fun provideRunningRecordsComponent() = runningRecordsComponent
}