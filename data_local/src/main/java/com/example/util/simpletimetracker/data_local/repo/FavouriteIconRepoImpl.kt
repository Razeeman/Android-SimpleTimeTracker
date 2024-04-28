package com.example.util.simpletimetracker.data_local.repo

import com.example.util.simpletimetracker.data_local.database.FavouriteIconDao
import com.example.util.simpletimetracker.data_local.mapper.FavouriteIconDataLocalMapper
import com.example.util.simpletimetracker.data_local.utils.removeIf
import com.example.util.simpletimetracker.data_local.utils.withLockedCache
import com.example.util.simpletimetracker.domain.model.FavouriteIcon
import com.example.util.simpletimetracker.domain.repo.FavouriteIconRepo
import kotlinx.coroutines.sync.Mutex
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavouriteIconRepoImpl @Inject constructor(
    private val dao: FavouriteIconDao,
    private val mapper: FavouriteIconDataLocalMapper,
) : FavouriteIconRepo {

    private var cache: List<FavouriteIcon>? = null
    private val mutex: Mutex = Mutex()

    override suspend fun getAll(): List<FavouriteIcon> = mutex.withLockedCache(
        logMessage = "getAll",
        accessCache = { cache },
        accessSource = { dao.getAll().map(mapper::map) },
        afterSourceAccess = { cache = it },
    )

    override suspend fun get(id: Long): FavouriteIcon? = mutex.withLockedCache(
        logMessage = "get id",
        accessCache = { cache?.firstOrNull { it.id == id } },
        accessSource = { dao.get(id)?.let(mapper::map) },
    )

    override suspend fun get(icon: String): FavouriteIcon? = mutex.withLockedCache(
        logMessage = "get name",
        accessCache = { cache?.firstOrNull { it.icon == icon } },
        accessSource = { dao.get(icon)?.let(mapper::map) },
    )

    override suspend fun add(favouriteIcon: FavouriteIcon): Long = mutex.withLockedCache(
        logMessage = "add",
        accessSource = { dao.insert(favouriteIcon.let(mapper::map)) },
        afterSourceAccess = { cache = null },
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