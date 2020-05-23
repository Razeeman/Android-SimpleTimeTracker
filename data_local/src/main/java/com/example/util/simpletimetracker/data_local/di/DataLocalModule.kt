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
import com.example.util.simpletimetracker.data_local.resolver.BackupRepoImpl
import com.example.util.simpletimetracker.domain.di.AppContext
import com.example.util.simpletimetracker.domain.repo.RecordCacheRepo
import com.example.util.simpletimetracker.domain.repo.RecordRepo
import com.example.util.simpletimetracker.domain.repo.RecordTypeRepo
import com.example.util.simpletimetracker.domain.repo.RunningRecordRepo
import com.example.util.simpletimetracker.domain.resolver.BackupRepo
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

    @Provides
    fun getRecordTypeRepo(recordTypeRepoImpl: RecordTypeRepoImpl): RecordTypeRepo {
        return recordTypeRepoImpl
    }

    @Provides
    @Singleton
    fun getRunningRecordDao(database: AppDatabase): RunningRecordDao {
        return database.runningRecordDao()
    }

    @Provides
    fun getRunningRecordRepo(runningRecordRepoImpl: RunningRecordRepoImpl): RunningRecordRepo {
        return runningRecordRepoImpl
    }

    @Provides
    @Singleton
    fun getBackupResolver(backupResolverImpl: BackupRepoImpl): BackupRepo {
        return backupResolverImpl
    }
}