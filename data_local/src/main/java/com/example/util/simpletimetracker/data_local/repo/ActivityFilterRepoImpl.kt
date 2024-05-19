package com.example.util.simpletimetracker.data_local.repo

import com.example.util.simpletimetracker.data_local.database.ActivityFilterDao
import com.example.util.simpletimetracker.data_local.utils.withLockedCache
import com.example.util.simpletimetracker.data_local.mapper.ActivityFilterDataLocalMapper
import com.example.util.simpletimetracker.data_local.utils.removeIf
import com.example.util.simpletimetracker.domain.model.ActivityFilter
import com.example.util.simpletimetracker.domain.repo.ActivityFilterRepo
import kotlinx.coroutines.sync.Mutex
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityFilterRepoImpl @Inject constructor(
    private val activityFilterDao: ActivityFilterDao,
    private val activityFilterDataLocalMapper: ActivityFilterDataLocalMapper,
) : ActivityFilterRepo {

    private var cache: List<ActivityFilter>? = null
    private val mutex: Mutex = Mutex()

    override suspend fun getAll(): List<ActivityFilter> = mutex.withLockedCache(
        logMessage = "getAll",
        accessCache = { cache },
        accessSource = { activityFilterDao.getAll().map(activityFilterDataLocalMapper::map) },
        afterSourceAccess = { cache = it },
    )

    override suspend fun get(id: Long): ActivityFilter? = mutex.withLockedCache(
        logMessage = "get",
        accessCache = { cache?.firstOrNull { it.id == id } },
        accessSource = { activityFilterDao.get(id)?.let(activityFilterDataLocalMapper::map) },
    )

    override suspend fun add(activityFilter: ActivityFilter): Long = mutex.withLockedCache(
        logMessage = "add",
        accessSource = { activityFilterDao.insert(activityFilter.let(activityFilterDataLocalMapper::map)) },
        afterSourceAccess = { cache = null },
    )

    override suspend fun changeSelected(id: Long, selected: Boolean) = mutex.withLockedCache(
        logMessage = "changeSelected",
        accessSource = { activityFilterDao.changeSelected(id, if (selected) 1 else 0) },
        afterSourceAccess = { cache = cache?.map { if (it.id == id) it.copy(selected = selected) else it } },
    )

    override suspend fun changeSelectedAll(selected: Boolean) = mutex.withLockedCache(
        logMessage = "changeSelectedAll",
        accessSource = { activityFilterDao.changeSelectedAll(if (selected) 1 else 0) },
        afterSourceAccess = { cache = cache?.map { it.copy(selected = selected) } },
    )

    override suspend fun remove(id: Long) = mutex.withLockedCache(
        logMessage = "remove",
        accessSource = { activityFilterDao.delete(id) },
        afterSourceAccess = { cache = cache?.removeIf { it.id == id } },
    )

    override suspend fun clear() = mutex.withLockedCache(
        logMessage = "clear",
        accessSource = { activityFilterDao.clear() },
        afterSourceAccess = { cache = null },
    )
}