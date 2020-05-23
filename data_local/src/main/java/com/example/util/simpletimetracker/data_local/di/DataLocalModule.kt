package com.example.util.simpletimetracker.data_local.di

import android.content.Context
import androidx.room.Room
import com.example.util.simpletimetracker.data_local.database.AppDatabase
import com.example.util.simpletimetracker.data_local.database.RecordDao
import com.example.util.simpletimetracker.data_local.database.RecordTypeDao
import com.example.util.simpletimetracker.data_local.database.RunningRecordDao
import com.example.util.simpletimetracker.data_local.repo.RecordCacheRepoImpl
import com.example.util.simpletimetracker.data_local.repo.RecordRepoImpl
import com.example.util.simpletimetracker.data_local.repo.RecordTypeRepoImpl
import com.example.util.simpletimetracker.data_local.repo.RunningRecordRepoImpl
import com.example.util.simpletimetracker.domain.di.AppContext
import com.example.util.simpletimetracker.domain.repo.RecordCacheRepo
import com.example.util.simpletimetracker.domain.repo.RecordRepo
import com.example.util.simpletimetracker.domain.repo.RecordTypeRepo
import com.example.util.simpletimetracker.domain.repo.RunningRecordRepo
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DataLocalModule {

    @Provides
    @Singleton
    fun getAppDatabase(@AppContext context: Context): AppDatabase {
        return Room
            .databaseBuilder(
                context,
                AppDatabase::class.java, AppDatabase.DATABASE_NAME
            )
            .build()
    }

    @Provides
    @Singleton
    fun getRecordDao(database: AppDatabase): RecordDao {
        return database.recordDao()
    }

    // TODO binds?
    @Provides
    fun getRecordRepo(recordRepoImpl: RecordRepoImpl): RecordRepo {
        return recordRepoImpl
    }

    @Provides
    fun getRecordCacheRepo(recordCacheRepoImpl: RecordCacheRepoImpl): RecordCacheRepo {
        return recordCacheRepoImpl
    }

    @Provides
    @Singleton
    fun getRecordTypeDao(database: AppDatabase): RecordTypeDao {
        return database.recordTypeDao()
    }

    // TODO binds?
    @Provides
    fun getRecordTypeRepo(recordTypeRepoImpl: RecordTypeRepoImpl): RecordTypeRepo {
        return recordTypeRepoImpl
    }

    @Provides
    @Singleton
    fun getRunningRecordDao(database: AppDatabase): RunningRecordDao {
        return database.runningRecordDao()
    }

    // TODO binds?
    @Provides
    fun getRunningRecordRepo(runningRecordRepoImpl: RunningRecordRepoImpl): RunningRecordRepo {
        return runningRecordRepoImpl
    }
}