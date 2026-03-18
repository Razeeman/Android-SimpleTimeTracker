package com.example.util.simpletimetracker.data_local.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.example.util.simpletimetracker.core.extension.allowDiskWrite
import com.example.util.simpletimetracker.data_local.activityFilter.ActivityFilterDao
import com.example.util.simpletimetracker.data_local.activitySuggestion.ActivitySuggestionDao
import com.example.util.simpletimetracker.data_local.database.AppDatabase
import com.example.util.simpletimetracker.data_local.database.AppDatabaseMigrations
import com.example.util.simpletimetracker.data_local.category.CategoryDao
import com.example.util.simpletimetracker.data_local.complexRule.ComplexRulesDao
import com.example.util.simpletimetracker.data_local.favourite.FavouriteColorDao
import com.example.util.simpletimetracker.data_local.favourite.FavouriteCommentDao
import com.example.util.simpletimetracker.data_local.favourite.FavouriteIconDao
import com.example.util.simpletimetracker.data_local.favourite.RecordTypeToFavouriteCommentDao
import com.example.util.simpletimetracker.data_local.record.RecordDao
import com.example.util.simpletimetracker.data_local.recordTag.RecordTagDao
import com.example.util.simpletimetracker.data_local.recordTag.RecordToRecordTagDao
import com.example.util.simpletimetracker.data_local.category.RecordTypeCategoryDao
import com.example.util.simpletimetracker.data_local.durationSuggestion.DurationSuggestionDao
import com.example.util.simpletimetracker.data_local.recordType.RecordTypeDao
import com.example.util.simpletimetracker.data_local.recordType.RecordTypeGoalDao
import com.example.util.simpletimetracker.data_local.recordTag.RecordTypeToDefaultTagDao
import com.example.util.simpletimetracker.data_local.recordTag.RecordTypeToTagDao
import com.example.util.simpletimetracker.data_local.record.RunningRecordDao
import com.example.util.simpletimetracker.data_local.recordShortcut.RecordShortcutDao
import com.example.util.simpletimetracker.data_local.recordTag.RecordShortcutToRecordTagDao
import com.example.util.simpletimetracker.data_local.recordTag.RunningRecordToRecordTagDao
import com.example.util.simpletimetracker.data_local.recordsFilter.FavouriteRecordsFilterDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DataLocalModule {

    companion object {
        private const val PREFS_NAME = "prefs_simple_time_tracker"
    }

    @Provides
    @Singleton
    fun getAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room
            .databaseBuilder(
                context,
                AppDatabase::class.java, AppDatabase.DATABASE_NAME,
            )
            .addMigrations(
                *AppDatabaseMigrations.migrations.toTypedArray(),
            )
            .build()
    }

    @Provides
    @Singleton
    fun getSharedPrefs(@ApplicationContext context: Context): SharedPreferences {
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

    @Provides
    @Singleton
    fun getRecordTagDao(database: AppDatabase): RecordTagDao {
        return database.recordTagDao()
    }

    @Provides
    @Singleton
    fun getRecordToRecordTagDao(database: AppDatabase): RecordToRecordTagDao {
        return database.recordToRecordTagDao()
    }

    @Provides
    @Singleton
    fun getRunningRecordToRecordTagDao(database: AppDatabase): RunningRecordToRecordTagDao {
        return database.runningRecordToRecordTagDao()
    }

    @Provides
    @Singleton
    fun getRecordShortcutToRecordTagDao(database: AppDatabase): RecordShortcutToRecordTagDao {
        return database.recordShortcutToRecordTagDao()
    }

    @Provides
    @Singleton
    fun getRecordTypeToTagDao(database: AppDatabase): RecordTypeToTagDao {
        return database.recordTypeToTagDao()
    }

    @Provides
    @Singleton
    fun getRecordTypeToDefaultTagDao(database: AppDatabase): RecordTypeToDefaultTagDao {
        return database.recordTypeToDefaultTagDao()
    }

    @Provides
    @Singleton
    fun getActivityFilterDao(database: AppDatabase): ActivityFilterDao {
        return database.activityFilterDao()
    }

    @Provides
    @Singleton
    fun getActivitySuggestionDao(database: AppDatabase): ActivitySuggestionDao {
        return database.activitySuggestionDao()
    }

    @Provides
    @Singleton
    fun getFavouriteCommentDao(database: AppDatabase): FavouriteCommentDao {
        return database.favouriteCommentDao()
    }

    @Provides
    @Singleton
    fun getRecordTypeToFavouriteCommentDao(database: AppDatabase): RecordTypeToFavouriteCommentDao {
        return database.recordTypeToFavouriteCommentDao()
    }

    @Provides
    @Singleton
    fun getFavouriteColorDao(database: AppDatabase): FavouriteColorDao {
        return database.favouriteColorDao()
    }

    @Provides
    @Singleton
    fun getFavouriteIconDao(database: AppDatabase): FavouriteIconDao {
        return database.favouriteIconDao()
    }

    @Provides
    @Singleton
    fun getRecordTypeGoalDao(database: AppDatabase): RecordTypeGoalDao {
        return database.recordTypeGoalDao()
    }

    @Provides
    @Singleton
    fun getComplexRulesDao(database: AppDatabase): ComplexRulesDao {
        return database.complexRulesDao()
    }

    @Provides
    @Singleton
    fun getDurationSuggestionDao(database: AppDatabase): DurationSuggestionDao {
        return database.durationSuggestionDao()
    }

    @Provides
    @Singleton
    fun getRecordShortcutDao(database: AppDatabase): RecordShortcutDao {
        return database.recordShortcutDao()
    }

    @Provides
    @Singleton
    fun getFavouriteRecordsFilterDao(database: AppDatabase): FavouriteRecordsFilterDao {
        return database.favouriteRecordsFilterDao()
    }
}