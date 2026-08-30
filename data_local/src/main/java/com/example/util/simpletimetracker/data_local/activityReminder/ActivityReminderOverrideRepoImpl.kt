package com.example.util.simpletimetracker.data_local.activityReminder

import com.example.util.simpletimetracker.data_local.base.withLockedCache
import com.example.util.simpletimetracker.domain.activityReminder.model.ActivityReminderOverride
import com.example.util.simpletimetracker.domain.activityReminder.repo.ActivityReminderOverrideRepo
import com.example.util.simpletimetracker.domain.extension.removeIf
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex

@Singleton
class ActivityReminderOverrideRepoImpl @Inject constructor(
    private val dao: ActivityReminderOverrideDao,
    private val mapper: ActivityReminderDataLocalMapper,
) : ActivityReminderOverrideRepo {

    private var cache: List<ActivityReminderOverride>? = null
    private val mutex: Mutex = Mutex()

    override suspend fun getAll(): List<ActivityReminderOverride> = mutex.withLockedCache(
        logMessage = "getAll",
        accessCache = { cache },
        accessSource = { dao.getAll().mapNotNull(mapper::map) },
        afterSourceAccess = { cache = it },
    )

    override suspend fun get(activityId: Long): ActivityReminderOverride? = mutex.withLockedCache(
        logMessage = "get",
        accessCache = { cache?.firstOrNull { it.activityId == activityId } },
        accessSource = { dao.get(activityId)?.let(mapper::map) },
    )

    override suspend fun save(data: ActivityReminderOverride) = mutex.withLockedCache(
        logMessage = "save",
        accessSource = { dao.save(mapper.map(data)) },
        afterSourceAccess = { cache = null },
    )

    override suspend fun restoreFromBackup(data: List<ActivityReminderOverride>) = mutex.withLockedCache(
        logMessage = "restoreFromBackup",
        accessSource = { dao.restore(data.map(mapper::map)) },
        afterSourceAccess = { cache = null },
    )

    override suspend fun remove(activityId: Long) = mutex.withLockedCache(
        logMessage = "remove",
        accessSource = { dao.remove(activityId) },
        afterSourceAccess = { cache = cache?.removeIf { it.activityId == activityId } },
    )

    override suspend fun clear() = mutex.withLockedCache(
        logMessage = "clear",
        accessSource = { dao.clearOverrides() },
        afterSourceAccess = { cache = null },
    )
}
