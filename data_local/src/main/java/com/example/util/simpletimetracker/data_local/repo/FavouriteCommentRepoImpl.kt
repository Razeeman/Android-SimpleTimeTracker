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
    private val favouriteCommentDao: FavouriteCommentDao,
    private val FavouriteCommentDataLocalMapper: FavouriteCommentDataLocalMapper
) : FavouriteCommentRepo {

    override suspend fun getAll(): List<FavouriteComment> = withContext(Dispatchers.IO) {
        logDataAccess("getAll")
        favouriteCommentDao.getAll().map(FavouriteCommentDataLocalMapper::map)
    }

    override suspend fun get(id: Long): FavouriteComment? = withContext(Dispatchers.IO) {
        logDataAccess("get id")
        favouriteCommentDao.get(id)?.let(FavouriteCommentDataLocalMapper::map)
    }

    override suspend fun get(text: String): FavouriteComment? = withContext(Dispatchers.IO) {
        logDataAccess("get text")
        favouriteCommentDao.get(text)?.let(FavouriteCommentDataLocalMapper::map)
    }

    override suspend fun add(comment: FavouriteComment): Long = withContext(Dispatchers.IO) {
        logDataAccess("add")
        return@withContext favouriteCommentDao.insert(
            comment.let(FavouriteCommentDataLocalMapper::map)
        )
    }

    override suspend fun remove(id: Long) = withContext(Dispatchers.IO) {
        logDataAccess("remove")
        favouriteCommentDao.delete(id)
    }

    override suspend fun clear() = withContext(Dispatchers.IO) {
        logDataAccess("clear")
        favouriteCommentDao.clear()
    }
}