package com.example.util.simpletimetracker.data_local.repo

import com.example.util.simpletimetracker.data_local.database.FavouriteColorDao
import com.example.util.simpletimetracker.data_local.mapper.FavouriteColorDataLocalMapper
import com.example.util.simpletimetracker.data_local.utils.logDataAccess
import com.example.util.simpletimetracker.domain.model.FavouriteColor
import com.example.util.simpletimetracker.domain.repo.FavouriteColorRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavouriteColorRepoImpl @Inject constructor(
    private val dao: FavouriteColorDao,
    private val mapper: FavouriteColorDataLocalMapper,
) : FavouriteColorRepo {

    override suspend fun getAll(): List<FavouriteColor> = withContext(Dispatchers.IO) {
        logDataAccess("getAll")
        dao.getAll().map(mapper::map)
    }

    override suspend fun get(id: Long): FavouriteColor? = withContext(Dispatchers.IO) {
        logDataAccess("get id")
        dao.get(id)?.let(mapper::map)
    }

    override suspend fun get(text: String): FavouriteColor? = withContext(Dispatchers.IO) {
        logDataAccess("get text")
        dao.get(text)?.let(mapper::map)
    }

    override suspend fun add(comment: FavouriteColor): Long = withContext(Dispatchers.IO) {
        logDataAccess("add")
        return@withContext dao.insert(
            comment.let(mapper::map),
        )
    }

    override suspend fun remove(id: Long) = withContext(Dispatchers.IO) {
        logDataAccess("remove")
        dao.delete(id)
    }

    override suspend fun clear() = withContext(Dispatchers.IO) {
        logDataAccess("clear")
        dao.clear()
    }
}