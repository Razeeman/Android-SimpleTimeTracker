package com.example.util.simpletimetracker

import android.app.Application
import com.example.util.simpletimetracker.di.AppComponent
import com.example.util.simpletimetracker.di.AppModule
import com.example.util.simpletimetracker.di.DaggerAppComponent
import com.example.util.simpletimetracker.di.FeatureComponentProvider
import com.example.util.simpletimetracker.feature_records.di.RecordsComponent
import com.example.util.simpletimetracker.feature_running_records.di.RunningRecordsComponent

class TimeTrackerApp : Application(), FeatureComponentProvider {

    // TODO provide in the function right away
    override var appComponent: AppComponent? = null
    private var runningRecordsComponent: RunningRecordsComponent? = null
    private var recordsComponent: RecordsComponent? = null

    override fun onCreate() {
        super.onCreate()

        appComponent = DaggerAppComponent.builder()
            .appModule(AppModule(this))
            .build()
        runningRecordsComponent = appComponent
            ?.plusRunningRecordsComponent()
        recordsComponent = appComponent
            ?.plusRecordsComponent()
    }

    override fun provideRunningRecordsComponent() = runningRecordsComponent

    override fun provideRecordsComponent() = recordsComponent
}