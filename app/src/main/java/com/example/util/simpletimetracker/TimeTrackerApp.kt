package com.example.util.simpletimetracker

import android.app.Application
import com.example.util.simpletimetracker.di.AppComponent
import com.example.util.simpletimetracker.di.AppModule
import com.example.util.simpletimetracker.di.DaggerAppComponent
import com.example.util.simpletimetracker.di.FeatureComponentProvider
import com.example.util.simpletimetracker.feature_records.di.RecordsComponent
import com.example.util.simpletimetracker.feature_running_records.di.RunningRecordsComponent

class TimeTrackerApp : Application(), FeatureComponentProvider {

    override var appComponent: AppComponent? = null
    override var runningRecordsComponent: RunningRecordsComponent? = null
    override var recordsComponent: RecordsComponent? = null

    override fun onCreate() {
        super.onCreate()
        initDi()
    }

    private fun initDi() {
        appComponent = DaggerAppComponent
            .builder()
            .appModule(AppModule(this))
            .build()
        runningRecordsComponent = appComponent?.plusRunningRecordsComponent()
        recordsComponent = appComponent?.plusRecordsComponent()
    }
}