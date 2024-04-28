package com.example.util.simpletimetracker.data_local.repo

import com.example.util.simpletimetracker.data_local.database.FavouriteCommentDao
import com.example.util.simpletimetracker.data_local.mapper.FavouriteCommentDataLocalMapper
import com.example.util.simpletimetracker.data_local.utils.logDataAccess
import com.example.util.simpletimetracker.domain.model.FavouriteComment
import com.example.util.simpletimetracker.domain.repo.FavouriteCommentRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavouriteCommentRepoImpl @Inject constructor(
    private val dao: FavouriteCommentDao,
    private val mapper: FavouriteCommentDataLocalMapper,
) : FavouriteCommentRepo {

    override suspend fun getAll(): List<FavouriteComment> = withContext(Dispatchers.IO) {
        logDataAccess("getAll")
        dao.getAll().map(mapper::map)
    }

    override suspend fun get(id: Long): FavouriteComment? = withContext(Dispatchers.IO) {
        logDataAccess("get id")
        dao.get(id)?.let(mapper::map)
    }

    override suspend fun get(text: String): FavouriteComment? = withContext(Dispatchers.IO) {
        logDataAccess("get text")
        dao.get(text)?.let(mapper::map)
    }

    override suspend fun add(comment: FavouriteComment): Long = withContext(Dispatchers.IO) {
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