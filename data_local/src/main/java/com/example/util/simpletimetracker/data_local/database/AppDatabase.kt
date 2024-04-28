package com.example.util.simpletimetracker.data_local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.util.simpletimetracker.data_local.model.ActivityFilterDBO
import com.example.util.simpletimetracker.data_local.model.CategoryDBO
import com.example.util.simpletimetracker.data_local.model.FavouriteCommentDBO
import com.example.util.simpletimetracker.data_local.model.FavouriteIconDBO
import com.example.util.simpletimetracker.data_local.model.RecordDBO
import com.example.util.simpletimetracker.data_local.model.RecordTagDBO
import com.example.util.simpletimetracker.data_local.model.RecordToRecordTagDBO
import com.example.util.simpletimetracker.data_local.model.RecordTypeCategoryDBO
import com.example.util.simpletimetracker.data_local.model.RecordTypeDBO
import com.example.util.simpletimetracker.data_local.model.RecordTypeGoalDBO
import com.example.util.simpletimetracker.data_local.model.RecordTypeToDefaultTagDBO
import com.example.util.simpletimetracker.data_local.model.RecordTypeToTagDBO
import com.example.util.simpletimetracker.data_local.model.RunningRecordDBO
import com.example.util.simpletimetracker.data_local.model.RunningRecordToRecordTagDBO

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
        RecordTypeToTagDBO::class,
        RecordTypeToDefaultTagDBO::class,
        ActivityFilterDBO::class,
        FavouriteCommentDBO::class,
        RecordTypeGoalDBO::class,
        FavouriteIconDBO::class,
    ],
    version = 18,
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

    abstract fun recordTypeToTagDao(): RecordTypeToTagDao

    abstract fun recordTypeToDefaultTagDao(): RecordTypeToDefaultTagDao

    abstract fun activityFilterDao(): ActivityFilterDao

    abstract fun favouriteCommentDao(): FavouriteCommentDao

    abstract fun recordTypeGoalDao(): RecordTypeGoalDao

    abstract fun favouriteIconDao(): FavouriteIconDao

    companion object {
        const val DATABASE_NAME = "simpleTimeTrackerDB"
    }
}