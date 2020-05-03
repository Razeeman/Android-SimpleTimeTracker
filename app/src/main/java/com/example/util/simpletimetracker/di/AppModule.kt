package com.example.util.simpletimetracker.di

import android.content.Context
import com.example.util.simpletimetracker.TimeTrackerApp
import com.example.util.simpletimetracker.domain.di.AppContext
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule(application: TimeTrackerApp) {

    private var appContext: Context = application.applicationContext

    @Provides
    @Singleton
    @AppContext
    fun getAppContext(): Context {
        return appContext
    }
}