package com.example.util.simpletimetracker.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query


@Dao
interface TimePeriodDao {

    @Query("SELECT * FROM timePeriods")
    fun getAll(): LiveData<List<TimePeriodDBO>>

    @Insert
    fun insert(timePeriod: TimePeriodDBO)
}