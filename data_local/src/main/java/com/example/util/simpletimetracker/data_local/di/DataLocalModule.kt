package com.example.util.simpletimetracker.data_local.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.example.util.simpletimetracker.core.extension.allowDiskWrite
import com.example.util.simpletimetracker.data_local.database.AppDatabase
import com.example.util.simpletimetracker.data_local.database.AppDatabaseMigrations
import com.example.util.simpletimetracker.data_local.database.CategoryDao
import com.example.util.simpletimetracker.data_local.database.RecordDao
import com.example.util.simpletimetracker.data_local.database.RecordTypeCategoryDao
import com.example.util.simpletimetracker.data_local.database.RecordTypeDao
import com.example.util.simpletimetracker.data_local.database.RunningRecordDao
import com.example.util.simpletimetracker.data_local.repo.CategoryRepoImpl
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl
import com.example.util.simpletimetracker.data_local.repo.RecordCacheRepoImpl
import com.example.util.simpletimetracker.data_local.repo.RecordRepoImpl
import com.example.util.simpletimetracker.data_local.repo.RecordTypeCacheRepoImpl
import com.example.util.simpletimetracker.data_local.repo.RecordTypeCategoryRepoImpl
import com.example.util.simpletimetracker.data_local.repo.RecordTypeRepoImpl
import com.example.util.simpletimetracker.data_local.repo.RunningRecordRepoImpl
import com.example.util.simpletimetracker.data_local.resolver.BackupRepoImpl
import com.example.util.simpletimetracker.data_local.resolver.CsvRepoImpl
import com.example.util.simpletimetracker.domain.di.AppContext
import com.example.util.simpletimetracker.domain.repo.CategoryRepo
import com.example.util.simpletimetracker.domain.repo.PrefsRepo
import com.example.util.simpletimetracker.domain.repo.RecordCacheRepo
import com.example.util.simpletimetracker.domain.repo.RecordRepo
import com.example.util.simpletimetracker.domain.repo.RecordTypeCacheRepo
import com.example.util.simpletimetracker.domain.repo.RecordTypeCategoryRepo
import com.example.util.simpletimetracker.domain.repo.RecordTypeRepo
import com.example.util.simpletimetracker.domain.repo.RunningRecordRepo
import com.example.util.simpletimetracker.domain.resolver.BackupRepo
import com.example.util.simpletimetracker.domain.resolver.CsvRepo
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
            .addMigrations(
                AppDatabaseMigrations.migration_1_2,
                AppDatabaseMigrations.migration_2_3,
                AppDatabaseMigrations.migration_3_4
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

    @Provides
    @Singleton
    fun getCategoryDao(database: AppDatabase): CategoryDao {
        return database.categoryDao()
    }

    @Provides
    @Singleton
    fun getRecordTypeCategoryDao(database: AppDatabase): RecordTypeCategoryDao {
        return database.recordTypeCategoryDao()
    }

    @Module
    abstract inner class DataLocalModuleBinds {
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
        abstract fun getRecordTypeCacheRepo(impl: RecordTypeCacheRepoImpl): RecordTypeCacheRepo

        @Binds
        @Singleton
        abstract fun getRunningRecordRepo(impl: RunningRecordRepoImpl): RunningRecordRepo

        @Binds
        @Singleton
        abstract fun getPrefsRepo(impl: PrefsRepoImpl): PrefsRepo

        @Binds
        @Singleton
        abstract fun getBackupRepo(impl: BackupRepoImpl): BackupRepo

        @Binds
        @Singleton
        abstract fun getCsvRepo(impl: CsvRepoImpl): CsvRepo

        @Binds
        @Singleton
        abstract fun getCategoryRepo(impl: CategoryRepoImpl): CategoryRepo

        @Binds
        @Singleton
        abstract fun getRecordTypeCategoryRepo(impl: RecordTypeCategoryRepoImpl): RecordTypeCategoryRepo
    }
}