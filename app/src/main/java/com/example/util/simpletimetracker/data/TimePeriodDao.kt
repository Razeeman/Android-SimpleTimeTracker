package com.example.util.simpletimetracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TimePeriodDao {

    @Query("SELECT * FROM timePeriods")
    suspend fun getAll(): List<TimePeriodDBO>

    @Insert
    suspend fun insert(timePeriod: TimePeriodDBO)
}