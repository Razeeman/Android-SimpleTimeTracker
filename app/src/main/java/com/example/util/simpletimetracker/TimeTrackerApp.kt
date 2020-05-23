package com.example.util.simpletimetracker

import android.app.Application
import com.example.util.simpletimetracker.di.AppComponent
import com.example.util.simpletimetracker.di.AppModule
import com.example.util.simpletimetracker.di.DaggerAppComponent
import com.example.util.simpletimetracker.di.FeatureComponentProvider
import com.example.util.simpletimetracker.feature_change_record.di.ChangeRecordComponent
import com.example.util.simpletimetracker.feature_change_record_type.di.ChangeRecordTypeComponent
import com.example.util.simpletimetracker.feature_records.di.RecordsComponent
import com.example.util.simpletimetracker.feature_running_records.di.RunningRecordsComponent
import com.example.util.simpletimetracker.feature_statistics.di.StatisticsComponent
import timber.log.Timber
import timber.log.Timber.DebugTree

class TimeTrackerApp : Application(), FeatureComponentProvider {

    override var appComponent: AppComponent? = null
    override var runningRecordsComponent: RunningRecordsComponent? = null
    override var changeRecordTypeComponent: ChangeRecordTypeComponent? = null
    override var recordsComponent: RecordsComponent? = null
    override var changeRecordComponent: ChangeRecordComponent? = null
    override var statisticsComponent: StatisticsComponent? = null

    override fun onCreate() {
        super.onCreate()
        initLog()
        initDi()
    }

    private fun initDi() {
        appComponent = DaggerAppComponent
            .builder()
            .appModule(AppModule(this))
            .build()
        runningRecordsComponent = appComponent?.plusRunningRecordsComponent()
        changeRecordTypeComponent = appComponent?.plusChangeRecordTypeComponent()
        recordsComponent = appComponent?.plusRecordsComponent()
        changeRecordComponent = appComponent?.plusChangeRecordComponent()
        statisticsComponent = appComponent?.plusStatisticsComponent()
    }

    private fun initLog() {
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }
    }
}