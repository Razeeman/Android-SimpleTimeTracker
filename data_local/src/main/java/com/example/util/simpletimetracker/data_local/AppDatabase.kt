package com.example.util.simpletimetracker.data_local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [RecordDBO::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun recordDao(): RecordDao

    companion object {
        const val DATABASE_NAME = "simpleTimeTrackerDB"
    }
}