package com.example.util.simpletimetracker.data_local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.util.simpletimetracker.data_local.model.RecordTypeGoalDBO

@Dao
interface RecordTypeGoalDao {

    @Query("SELECT * FROM recordTypeGoals")
    suspend fun getAll(): List<RecordTypeGoalDBO>

    @Query("SELECT * FROM recordTypeGoals WHERE type_id != 0")
    suspend fun getAllTypeGoals(): List<RecordTypeGoalDBO>

    @Query("SELECT * FROM recordTypeGoals WHERE category_id != 0")
    suspend fun getAllCategoryGoals(): List<RecordTypeGoalDBO>

    @Transaction
    @Query("SELECT * FROM recordTypeGoals WHERE type_id = :typeId")
    suspend fun getByType(typeId: Long): List<RecordTypeGoalDBO>

    @Transaction
    @Query("SELECT * FROM recordTypeGoals WHERE category_id = :categoryId")
    suspend fun getByCategory(categoryId: Long): List<RecordTypeGoalDBO>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recordTypeGoal: RecordTypeGoalDBO): Long

    @Query("DELETE FROM recordTypeGoals WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM recordTypeGoals WHERE type_id = :typeId")
    suspend fun deleteByType(typeId: Long)

    @Query("DELETE FROM recordTypeGoals WHERE type_id = :categoryId")
    suspend fun deleteByCategory(categoryId: Long)

    @Query("DELETE FROM recordTypeGoals")
    suspend fun clear()
}