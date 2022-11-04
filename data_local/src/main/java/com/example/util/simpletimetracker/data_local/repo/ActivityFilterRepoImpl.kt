package com.example.util.simpletimetracker.data_local.repo

import com.example.util.simpletimetracker.data_local.database.ActivityFilterDao
import com.example.util.simpletimetracker.data_local.mapper.ActivityFilterDataLocalMapper
import com.example.util.simpletimetracker.domain.model.ActivityFilter
import com.example.util.simpletimetracker.domain.repo.ActivityFilterRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityFilterRepoImpl @Inject constructor(
    private val activityFilterDao: ActivityFilterDao,
    private val activityFilterDataLocalMapper: ActivityFilterDataLocalMapper,
) : ActivityFilterRepo {

    override suspend fun getAll(): List<ActivityFilter> = withContext(Dispatchers.IO) {
        Timber.d("getAll")
        activityFilterDao.getAll()
            .map(activityFilterDataLocalMapper::map)
    }

    override suspend fun get(id: Long): ActivityFilter? = withContext(Dispatchers.IO) {
        Timber.d("get")
        activityFilterDao.get(id)
            ?.let(activityFilterDataLocalMapper::map)
    }

    override suspend fun add(activityFilter: ActivityFilter): Long = withContext(Dispatchers.IO) {
        Timber.d("add")
        return@withContext activityFilterDao.insert(
            activityFilter.let(activityFilterDataLocalMapper::map)
        )
    }

    override suspend fun changeSelected(id: Long, selected: Boolean) = withContext(Dispatchers.IO) {
        Timber.d("changeSelected")
        activityFilterDao.changeSelected(id, if (selected) 1 else 0)
    }

    override suspend fun remove(id: Long) = withContext(Dispatchers.IO) {
        Timber.d("remove")
        activityFilterDao.delete(id)
    }

    override suspend fun clear() = withContext(Dispatchers.IO) {
        Timber.d("clear")
        activityFilterDao.clear()
    }
}