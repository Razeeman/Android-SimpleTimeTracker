package com.example.util.simpletimetracker.data_local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.util.simpletimetracker.data_local.model.RecordDBO
import com.example.util.simpletimetracker.data_local.model.RecordTypeDBO

@Database(
    entities = [
        RecordDBO::class,
        RecordTypeDBO::class
    ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun recordDao(): RecordDao

    abstract fun recordTypeDao(): RecordTypeDao

    companion object {
        const val DATABASE_NAME = "simpleTimeTrackerDB"
    }
}