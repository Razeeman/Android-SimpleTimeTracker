package com.example.util.simpletimetracker.data_local.activityFilter

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface ActivityFilterDao {

    @Transaction
    @Query("SELECT * FROM activityFilters")
    suspend fun getAll(): List<ActivityFilterDBO>

    @Transaction
    @Query("SELECT * FROM activityFilters WHERE id = :id LIMIT 1")
    suspend fun get(id: Long): ActivityFilterDBO?

    // Type 0 limits the lookup to activity filters so overlapping category IDs are not matched.
    // SQLite's || operator concatenates strings;
    // comma boundaries ensure full ID matches (e.g. 1 must not match 11).
    @Transaction
    @Query(
        """
        SELECT * FROM activityFilters
        WHERE type = 0
        AND instr(
            ',' || selectedIds || ',',
            ',' || CAST(:typeId AS TEXT) || ','
        ) > 0
        """,
    )
    suspend fun getByTypeId(typeId: Long): List<ActivityFilterDBO>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(activityFilter: ActivityFilterDBO): Long

    @Query("UPDATE activityFilters SET selected =:selected  WHERE id = :id")
    suspend fun changeSelected(id: Long, selected: Int)

    @Query("UPDATE activityFilters SET selected =:selected")
    suspend fun changeSelectedAll(selected: Int)

    @Query("DELETE FROM activityFilters WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM activityFilters")
    suspend fun clear()
}