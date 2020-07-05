package com.example.util.simpletimetracker

import android.app.Application
import android.os.StrictMode
import com.example.util.simpletimetracker.di.AppComponent
import com.example.util.simpletimetracker.di.AppModule
import com.example.util.simpletimetracker.di.DaggerAppComponent
import com.example.util.simpletimetracker.di.FeatureComponentProvider
import com.example.util.simpletimetracker.feature_change_record.di.ChangeRecordComponent
import com.example.util.simpletimetracker.feature_change_record_type.di.ChangeRecordTypeComponent
import com.example.util.simpletimetracker.feature_change_running_record.di.ChangeRunningRecordComponent
import com.example.util.simpletimetracker.feature_dialogs.chartFilter.di.ChartFilterComponent
import com.example.util.simpletimetracker.feature_records.di.RecordsComponent
import com.example.util.simpletimetracker.feature_running_records.di.RunningRecordsComponent
import com.example.util.simpletimetracker.feature_settings.di.SettingsComponent
import com.example.util.simpletimetracker.feature_statistics.di.StatisticsComponent
import com.example.util.simpletimetracker.feature_widget.di.WidgetComponent
import timber.log.Timber
import timber.log.Timber.DebugTree

class TimeTrackerApp : Application(), FeatureComponentProvider {

    override var appComponent: AppComponent? = null
    override var runningRecordsComponent: RunningRecordsComponent? = null
    override var changeRecordTypeComponent: ChangeRecordTypeComponent? = null
    override var recordsComponent: RecordsComponent? = null
    override var changeRecordComponent: ChangeRecordComponent? = null
    override var changeRunningRecordComponent: ChangeRunningRecordComponent? = null
    override var statisticsComponent: StatisticsComponent? = null
    override var settingsComponent: SettingsComponent? = null
    override var chartFilterComponent: ChartFilterComponent? = null
    override var widgetComponent: WidgetComponent? = null

    override fun onCreate() {
        super.onCreate()
        initLog()
        initDi()
        initStrictMode()
    }

    private fun initLog() {
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }
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
        changeRunningRecordComponent = appComponent?.plusChangeRunningRecordComponent()
        statisticsComponent = appComponent?.plusStatisticsComponent()
        settingsComponent = appComponent?.plusSettingComponent()
        chartFilterComponent = appComponent?.plusChartFilterComponent()
        widgetComponent = appComponent?.plusWidgetComponent()
    }

    private fun initStrictMode() {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .penaltyDialog()
                    .build()
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build()
            )
        }
    }
}