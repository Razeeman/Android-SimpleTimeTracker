package com.example.util.simpletimetracker.data_local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.util.simpletimetracker.data_local.model.ComplexRuleDBO

@Dao
interface ComplexRulesDao {

    @Query("SELECT * FROM complexRules")
    suspend fun getAll(): List<ComplexRuleDBO>

    @Query("SELECT * FROM complexRules WHERE id = :id LIMIT 1")
    suspend fun get(id: Long): ComplexRuleDBO?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: ComplexRuleDBO): Long

    @Query("DELETE FROM complexRules WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM complexRules")
    suspend fun clear()
}