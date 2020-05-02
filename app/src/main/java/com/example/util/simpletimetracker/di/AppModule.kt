package com.example.util.simpletimetracker.di

import android.content.Context
import androidx.room.Room
import com.example.util.simpletimetracker.TimeTrackerApp
import com.example.util.simpletimetracker.data.AppDatabase
import com.example.util.simpletimetracker.data.TimePeriodDao
import com.example.util.simpletimetracker.data.TimePeriodRepo
import com.example.util.simpletimetracker.domain.BaseTimePeriodRepo
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

    @Provides
    @Singleton
    fun getAppDatabase(@AppContext context: Context): AppDatabase {
        return Room
            .databaseBuilder(
                context,
                AppDatabase::class.java, "simpleTimeTrackerDatabase"
            )
            .build()
    }

    @Provides
    @Singleton
    fun getTimerPeriodDao(database: AppDatabase): TimePeriodDao {
        return database.timePeriodDao()
    }

    // TODO binds?
    @Provides
    fun getTimePeriodRepo(timePeriodRepo: TimePeriodRepo): BaseTimePeriodRepo {
        return timePeriodRepo
    }
}