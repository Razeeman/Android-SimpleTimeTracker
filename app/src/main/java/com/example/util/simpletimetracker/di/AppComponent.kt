package com.example.util.simpletimetracker.di

import com.example.util.simpletimetracker.data_local.di.DataLocalModule
import com.example.util.simpletimetracker.feature_records.di.RecordsComponent
import com.example.util.simpletimetracker.feature_running_records.di.RunningRecordsComponent
import com.example.util.simpletimetracker.ui.MainActivity
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class,
        DataLocalModule::class
    ]
)
interface AppComponent {

    fun inject(mainActivity: MainActivity)

    fun plusRunningRecordsComponent(): RunningRecordsComponent
    fun plusRecordsComponent(): RecordsComponent
}