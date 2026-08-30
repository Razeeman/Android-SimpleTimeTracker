package com.example.util.simpletimetracker.data_local.activityReminder

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface ActivityReminderOverrideDao {

    @Transaction
    @Query("SELECT * FROM activityReminderOverrides ORDER BY activity_id")
    suspend fun getAll(): List<ActivityReminderOverrideWithRulesDBO>

    @Transaction
    @Query("SELECT * FROM activityReminderOverrides WHERE activity_id = :activityId LIMIT 1")
    suspend fun get(activityId: Long): ActivityReminderOverrideWithRulesDBO?

    @Query("SELECT * FROM activityReminderRules WHERE activity_id = :activityId ORDER BY id")
    suspend fun getRules(activityId: Long): List<ActivityReminderRuleDBO>

    @Transaction
    suspend fun save(data: ActivityReminderOverrideWithRulesDBO) {
        val activityId = data.override.activityId
        val existingRules = getRules(activityId)
        val submittedPositiveIds = data.rules.map { it.id }.filter { it > 0 }.toSet()

        val updatedOverridesCount = updateOverride(
            activityId = data.override.activityId,
            mode = data.override.mode,
        )
        // Nothing was updates - doesn't exist yet, need to create.
        if (updatedOverridesCount == 0) {
            insertOverride(data.override)
        }

        if (data.override.mode == MODE_DISABLED) {
            deleteRules(activityId)
        } else {
            // Remove non existent rules.
            existingRules.filterNot { it.id in submittedPositiveIds }.forEach { deleteRule(it.id) }

            data.rules.forEach { rule ->
                if (rule.id > 0) {
                    // Update existent.
                    updateRule(
                        id = rule.id,
                        activityId = rule.activityId,
                        durationSeconds = rule.durationSeconds,
                        recurrent = rule.recurrent,
                        weekdays = rule.weekdays,
                        doNotDisturbStartMillis = rule.doNotDisturbStartMillis,
                        doNotDisturbEndMillis = rule.doNotDisturbEndMillis,
                    )
                } else {
                    // Add new ones.
                    // Ids can be negative - replace with usual 0 to create a row.
                    insertRule(
                        data = rule.copy(id = 0),
                    )
                }
            }
        }
    }

    @Transaction
    suspend fun restore(data: List<ActivityReminderOverrideWithRulesDBO>) {
        clearOverrides()
        data.forEach { insertOverride(it.override) }
        data.flatMap { it.rules }.forEach { insertRule(it) }
    }

    @Query("UPDATE activityReminderOverrides SET mode = :mode WHERE activity_id = :activityId")
    suspend fun updateOverride(activityId: Long, mode: Int): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertOverride(data: ActivityReminderOverrideDBO)

    @Query(
        """
        UPDATE activityReminderRules SET
            duration_seconds = :durationSeconds,
            recurrent = :recurrent,
            weekdays = :weekdays,
            dnd_start_millis = :doNotDisturbStartMillis,
            dnd_end_millis = :doNotDisturbEndMillis
        WHERE id = :id AND activity_id = :activityId
        """,
    )
    suspend fun updateRule(
        id: Long,
        activityId: Long,
        durationSeconds: Long,
        recurrent: Boolean,
        weekdays: String,
        doNotDisturbStartMillis: Long,
        doNotDisturbEndMillis: Long,
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(data: ActivityReminderRuleDBO): Long

    @Query("DELETE FROM activityReminderRules WHERE id = :id")
    suspend fun deleteRule(id: Long)

    @Query("DELETE FROM activityReminderRules WHERE activity_id = :activityId")
    suspend fun deleteRules(activityId: Long)

    @Query("DELETE FROM activityReminderOverrides WHERE activity_id = :activityId")
    suspend fun remove(activityId: Long)

    @Query("DELETE FROM activityReminderOverrides")
    suspend fun clearOverrides()

    companion object {
        const val MODE_DISABLED = 0
        const val MODE_CUSTOM = 1
    }
}
