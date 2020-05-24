package com.example.util.simpletimetracker.data_local.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.example.util.simpletimetracker.core.extension.allowDiskWrite
import com.example.util.simpletimetracker.data_local.database.AppDatabase
import com.example.util.simpletimetracker.data_local.database.RecordDao
import com.example.util.simpletimetracker.data_local.database.RecordTypeDao
import com.example.util.simpletimetracker.data_local.database.RunningRecordDao
import com.example.util.simpletimetracker.data_local.repo.*
import com.example.util.simpletimetracker.data_local.resolver.BackupRepoImpl
import com.example.util.simpletimetracker.domain.di.AppContext
import com.example.util.simpletimetracker.domain.repo.*
import com.example.util.simpletimetracker.domain.resolver.BackupRepo
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(includes = [DataLocalModule.DataLocalModuleBinds::class])
class DataLocalModule {

    companion object {
        private const val PREFS_NAME = "prefs_simple_time_tracker"
    }

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
    fun getSharedPrefs(@AppContext context: Context): SharedPreferences {
        allowDiskWrite {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    @Provides
    @Singleton
    fun getRecordDao(database: AppDatabase): RecordDao {
        return database.recordDao()
    }

    @Provides
    @Singleton
    fun getRecordTypeDao(database: AppDatabase): RecordTypeDao {
        return database.recordTypeDao()
    }

    @Provides
    @Singleton
    fun getRunningRecordDao(database: AppDatabase): RunningRecordDao {
        return database.runningRecordDao()
    }

    @Module
    abstract inner class DataLocalModuleBinds() {
        @Binds
        @Singleton
        abstract fun getRecordRepo(impl: RecordRepoImpl): RecordRepo

        @Binds
        @Singleton
        abstract fun getRecordCacheRepo(impl: RecordCacheRepoImpl): RecordCacheRepo

        @Binds
        @Singleton
        abstract fun getRecordTypeRepo(impl: RecordTypeRepoImpl): RecordTypeRepo

        @Binds
        @Singleton
        abstract fun getRunningRecordRepo(impl: RunningRecordRepoImpl): RunningRecordRepo

        @Binds
        @Singleton
        abstract fun getPrefsRepo(impl: PrefsRepoImpl): PrefsRepo

        @Binds
        @Singleton
        abstract fun getBackupResolver(impl: BackupRepoImpl): BackupRepo
    }
}