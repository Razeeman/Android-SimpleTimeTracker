package com.example.util.simpletimetracker.data_local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.util.simpletimetracker.data_local.model.CategoryDBO

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories")
    suspend fun getAll(): List<CategoryDBO>

    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    suspend fun get(id: Long): CategoryDBO?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: CategoryDBO)

    @Query("DELETE FROM categories")
    suspend fun clear()
}