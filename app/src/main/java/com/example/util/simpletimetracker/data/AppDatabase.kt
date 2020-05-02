package com.example.util.simpletimetracker.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [TimePeriodDBO::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun timePeriodDao(): TimePeriodDao
}