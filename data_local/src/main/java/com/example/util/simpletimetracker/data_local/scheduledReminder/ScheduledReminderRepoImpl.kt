package com.example.util.simpletimetracker.data_local.scheduledReminder

import com.example.util.simpletimetracker.data_local.base.withLockedCache
import com.example.util.simpletimetracker.domain.extension.removeIf
import com.example.util.simpletimetracker.domain.scheduledReminder.model.ScheduledReminder
import com.example.util.simpletimetracker.domain.scheduledReminder.repo.ScheduledReminderRepo
import kotlinx.coroutines.sync.Mutex
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduledReminderRepoImpl @Inject constructor(
    private val dao: ScheduledReminderDao,
    private val mapper: ScheduledReminderDataLocalMapper,
) : ScheduledReminderRepo {

    private var cache: List<ScheduledReminder>? = null
    private val mutex: Mutex = Mutex()

    override suspend fun getAll(): List<ScheduledReminder> = mutex.withLockedCache(
        logMessage = "getAll",
        accessCache = { cache },
        accessSource = { dao.getAll().map(mapper::map) },
        afterSourceAccess = { cache = it },
    )

    override suspend fun get(id: Long): ScheduledReminder? = mutex.withLockedCache(
        logMessage = "get",
        accessCache = { cache?.firstOrNull { it.id == id } },
        accessSource = { dao.get(id)?.let(mapper::map) },
    )

    override suspend fun add(data: ScheduledReminder): Long = mutex.withLockedCache(
        logMessage = "add",
        accessSource = { dao.insert(mapper.map(data)) },
        afterSourceAccess = { cache = null },
    )

    override suspend fun setEnabled(id: Long, enabled: Boolean) = mutex.withLockedCache(
        logMessage = "setEnabled",
        accessSource = { dao.setEnabled(id, enabled) },
        afterSourceAccess = {
            cache = cache?.map { if (it.id == id) it.copy(enabled = enabled) else it }
        },
    )

    override suspend fun remove(id: Long) = mutex.withLockedCache(
        logMessage = "remove",
        accessSource = { dao.delete(id) },
        afterSourceAccess = { cache = cache?.removeIf { it.id == id } },
    )

    override suspend fun clear() = mutex.withLockedCache(
        logMessage = "clear",
        accessSource = { dao.clear() },
        afterSourceAccess = { cache = null },
    )
}
