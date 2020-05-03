package com.example.util.simpletimetracker.data_local.di

import android.content.Context
import androidx.room.Room
import com.example.util.simpletimetracker.data_local.database.AppDatabase
import com.example.util.simpletimetracker.data_local.database.RecordDao
import com.example.util.simpletimetracker.data_local.repo.RecordRepo
import com.example.util.simpletimetracker.domain.repo.BaseRecordRepo
import com.example.util.simpletimetracker.domain.di.AppContext
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DataLocalModule() {

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
    fun getRecordRepo(recordRepo: RecordRepo): BaseRecordRepo {
        return recordRepo
    }
}