package com.example.util.simpletimetracker.data_local.favourite

import com.example.util.simpletimetracker.data_local.base.withLockedCache
import com.example.util.simpletimetracker.domain.extension.removeIf
import com.example.util.simpletimetracker.domain.favourite.model.RecordTypeToFavouriteComment
import com.example.util.simpletimetracker.domain.favourite.repo.RecordTypeToFavouriteCommentRepo
import kotlinx.coroutines.sync.Mutex
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordTypeToFavouriteCommentRepoImpl @Inject constructor(
    private val dao: RecordTypeToFavouriteCommentDao,
    private val mapper: RecordTypeToFavouriteCommentDataLocalMapper,
) : RecordTypeToFavouriteCommentRepo {

    private var cache: List<RecordTypeToFavouriteComment>? = null
    private val mutex: Mutex = Mutex()

    override suspend fun getAll(): List<RecordTypeToFavouriteComment> = mutex.withLockedCache(
        logMessage = "getAll",
        accessCache = { cache },
        accessSource = { dao.getAll().map(mapper::map) },
        afterSourceAccess = { cache = it },
    )

    override suspend fun getCommentIdsByType(typeId: Long): Set<Long> = mutex.withLockedCache(
        logMessage = "getCommentIdsByType",
        accessCache = { cache?.filter { it.recordTypeId == typeId }?.map { it.commentId }?.toSet() },
        accessSource = { dao.getCommentIdsByType(typeId).toSet() },
    )

    override suspend fun getTypeIdsByComment(commentId: Long): Set<Long> = mutex.withLockedCache(
        logMessage = "getTypeIdsByComment",
        accessCache = { cache?.filter { it.commentId == commentId }?.map { it.recordTypeId }?.toSet() },
        accessSource = { dao.getTypeIdsByComment(commentId).toSet() },
    )

    override suspend fun add(recordTypeToFavouriteComment: RecordTypeToFavouriteComment) = mutex.withLockedCache(
        logMessage = "add",
        accessSource = {
            recordTypeToFavouriteComment
                .let(mapper::map)
                .let { dao.insert(listOf(it)) }
        },
        afterSourceAccess = { cache = null },
    )

    override suspend fun addTypes(commentId: Long, typeIds: List<Long>) = mutex.withLockedCache(
        logMessage = "addTypes",
        accessSource = {
            typeIds.map {
                mapper.map(typeId = it, commentId = commentId)
            }.let { dao.insert(it) }
        },
        afterSourceAccess = { cache = null },
    )

    override suspend fun addComments(typeId: Long, commentIds: List<Long>) = mutex.withLockedCache(
        logMessage = "addComments",
        accessSource = {
            commentIds.map {
                mapper.map(typeId = typeId, commentId = it)
            }.let { dao.insert(it) }
        },
        afterSourceAccess = { cache = null },
    )

    override suspend fun removeTypes(commentId: Long, typeIds: List<Long>) = mutex.withLockedCache(
        logMessage = "removeTypes",
        accessSource = {
            typeIds.map {
                mapper.map(typeId = it, commentId = commentId)
            }.let { dao.delete(it) }
        },
        afterSourceAccess = { cache = null },
    )

    override suspend fun removeAll(commentId: Long) = mutex.withLockedCache(
        logMessage = "removeAll",
        accessSource = { dao.deleteAll(commentId) },
        afterSourceAccess = { cache = cache?.removeIf { it.commentId == commentId } },
    )

    override suspend fun removeAllByType(typeId: Long) = mutex.withLockedCache(
        logMessage = "removeAllByType",
        accessSource = { dao.deleteAllByType(typeId) },
        afterSourceAccess = { cache = cache?.removeIf { it.recordTypeId == typeId } },
    )

    override suspend fun clear() = mutex.withLockedCache(
        logMessage = "clear",
        accessSource = { dao.clear() },
        afterSourceAccess = { cache = null },
    )
}