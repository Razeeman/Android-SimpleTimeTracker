package com.example.util.simpletimetracker.data_local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.util.simpletimetracker.data_local.activityFilter.ActivityFilterDBO
import com.example.util.simpletimetracker.data_local.activityFilter.ActivityFilterDao
import com.example.util.simpletimetracker.data_local.activitySuggestion.ActivitySuggestionDBO
import com.example.util.simpletimetracker.data_local.activitySuggestion.ActivitySuggestionDao
import com.example.util.simpletimetracker.data_local.category.CategoryDBO
import com.example.util.simpletimetracker.data_local.category.CategoryDao
import com.example.util.simpletimetracker.data_local.category.RecordTypeCategoryDBO
import com.example.util.simpletimetracker.data_local.category.RecordTypeCategoryDao
import com.example.util.simpletimetracker.data_local.complexRule.ComplexRuleDBO
import com.example.util.simpletimetracker.data_local.complexRule.ComplexRulesDao
import com.example.util.simpletimetracker.data_local.durationSuggestion.DurationSuggestionDBO
import com.example.util.simpletimetracker.data_local.durationSuggestion.DurationSuggestionDao
import com.example.util.simpletimetracker.data_local.favourite.FavouriteColorDBO
import com.example.util.simpletimetracker.data_local.favourite.FavouriteColorDao
import com.example.util.simpletimetracker.data_local.favourite.FavouriteCommentDBO
import com.example.util.simpletimetracker.data_local.favourite.FavouriteCommentDao
import com.example.util.simpletimetracker.data_local.favourite.FavouriteIconDBO
import com.example.util.simpletimetracker.data_local.favourite.FavouriteIconDao
import com.example.util.simpletimetracker.data_local.record.RecordDBO
import com.example.util.simpletimetracker.data_local.record.RecordDao
import com.example.util.simpletimetracker.data_local.record.RunningRecordDBO
import com.example.util.simpletimetracker.data_local.record.RunningRecordDao
import com.example.util.simpletimetracker.data_local.recordShortcut.RecordShortcutDBO
import com.example.util.simpletimetracker.data_local.recordShortcut.RecordShortcutDao
import com.example.util.simpletimetracker.data_local.recordTag.RecordShortcutToRecordTagDBO
import com.example.util.simpletimetracker.data_local.recordTag.RecordShortcutToRecordTagDao
import com.example.util.simpletimetracker.data_local.recordTag.RecordTagDBO
import com.example.util.simpletimetracker.data_local.recordTag.RecordTagDao
import com.example.util.simpletimetracker.data_local.recordTag.RecordToRecordTagDBO
import com.example.util.simpletimetracker.data_local.recordTag.RecordToRecordTagDao
import com.example.util.simpletimetracker.data_local.recordTag.RecordTypeToDefaultTagDBO
import com.example.util.simpletimetracker.data_local.recordTag.RecordTypeToDefaultTagDao
import com.example.util.simpletimetracker.data_local.recordTag.RecordTypeToTagDBO
import com.example.util.simpletimetracker.data_local.recordTag.RecordTypeToTagDao
import com.example.util.simpletimetracker.data_local.recordTag.RunningRecordToRecordTagDBO
import com.example.util.simpletimetracker.data_local.recordTag.RunningRecordToRecordTagDao
import com.example.util.simpletimetracker.data_local.recordType.RecordTypeDBO
import com.example.util.simpletimetracker.data_local.recordType.RecordTypeDao
import com.example.util.simpletimetracker.data_local.recordType.RecordTypeGoalDBO
import com.example.util.simpletimetracker.data_local.recordType.RecordTypeGoalDao
import com.example.util.simpletimetracker.data_local.recordsFilter.FavouriteRecordsFilterDBO
import com.example.util.simpletimetracker.data_local.recordsFilter.FavouriteRecordsFilterDao

@Database(
    entities = [
        RecordDBO::class,
        RecordTypeDBO::class,
        RunningRecordDBO::class,
        CategoryDBO::class,
        RecordTypeCategoryDBO::class,
        RecordTagDBO::class,
        RecordToRecordTagDBO::class,
        RunningRecordToRecordTagDBO::class,
        RecordShortcutToRecordTagDBO::class,
        RecordTypeToTagDBO::class,
        RecordTypeToDefaultTagDBO::class,
        ActivityFilterDBO::class,
        FavouriteCommentDBO::class,
        RecordTypeGoalDBO::class,
        FavouriteIconDBO::class,
        ComplexRuleDBO::class,
        FavouriteColorDBO::class,
        ActivitySuggestionDBO::class,
        DurationSuggestionDBO::class,
        RecordShortcutDBO::class,
        FavouriteRecordsFilterDBO.MainDBO::class,
        FavouriteRecordsFilterDBO.FilterDBO::class,
    ],
    version = 31,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun recordDao(): RecordDao

    abstract fun recordTypeDao(): RecordTypeDao

    abstract fun runningRecordDao(): RunningRecordDao

    abstract fun categoryDao(): CategoryDao

    abstract fun recordTypeCategoryDao(): RecordTypeCategoryDao

    abstract fun recordTagDao(): RecordTagDao

    abstract fun recordToRecordTagDao(): RecordToRecordTagDao

    abstract fun runningRecordToRecordTagDao(): RunningRecordToRecordTagDao

    abstract fun recordShortcutToRecordTagDao(): RecordShortcutToRecordTagDao

    abstract fun recordTypeToTagDao(): RecordTypeToTagDao

    abstract fun recordTypeToDefaultTagDao(): RecordTypeToDefaultTagDao

    abstract fun activityFilterDao(): ActivityFilterDao

    abstract fun activitySuggestionDao(): ActivitySuggestionDao

    abstract fun favouriteCommentDao(): FavouriteCommentDao

    abstract fun recordTypeGoalDao(): RecordTypeGoalDao

    abstract fun favouriteIconDao(): FavouriteIconDao

    abstract fun complexRulesDao(): ComplexRulesDao

    abstract fun favouriteColorDao(): FavouriteColorDao

    abstract fun durationSuggestionDao(): DurationSuggestionDao

    abstract fun recordShortcutDao(): RecordShortcutDao

    abstract fun favouriteRecordsFilterDao(): FavouriteRecordsFilterDao

    companion object {
        const val DATABASE_NAME = "simpleTimeTrackerDB"
    }
}