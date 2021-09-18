package com.example.util.simpletimetracker.data_local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.util.simpletimetracker.data_local.model.CategoryDBO
import com.example.util.simpletimetracker.data_local.model.RecordDBO
import com.example.util.simpletimetracker.data_local.model.RecordTagDBO
import com.example.util.simpletimetracker.data_local.model.RecordTypeCategoryDBO
import com.example.util.simpletimetracker.data_local.model.RecordTypeDBO
import com.example.util.simpletimetracker.data_local.model.RunningRecordDBO

@Database(
    entities = [
        RecordDBO::class,
        RecordTypeDBO::class,
        RunningRecordDBO::class,
        CategoryDBO::class,
        RecordTypeCategoryDBO::class,
        RecordTagDBO::class
    ],
    version = 6,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun recordDao(): RecordDao

    abstract fun recordTypeDao(): RecordTypeDao

    abstract fun runningRecordDao(): RunningRecordDao

    abstract fun categoryDao(): CategoryDao

    abstract fun recordTypeCategoryDao(): RecordTypeCategoryDao

    abstract fun recordTagDao(): RecordTagDao

    companion object {
        const val DATABASE_NAME = "simpleTimeTrackerDB"
    }
}