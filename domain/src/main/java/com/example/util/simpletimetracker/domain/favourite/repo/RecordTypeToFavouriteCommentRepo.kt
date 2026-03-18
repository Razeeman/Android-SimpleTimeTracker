package com.example.util.simpletimetracker.domain.favourite.repo

import com.example.util.simpletimetracker.domain.favourite.model.RecordTypeToFavouriteComment

interface RecordTypeToFavouriteCommentRepo {

    suspend fun getAll(): List<RecordTypeToFavouriteComment>

    suspend fun getCommentIdsByType(typeId: Long): Set<Long>

    suspend fun getTypeIdsByComment(commentId: Long): Set<Long>

    suspend fun add(recordTypeToFavouriteComment: RecordTypeToFavouriteComment)

    suspend fun addTypes(commentId: Long, typeIds: List<Long>)

    suspend fun addComments(typeId: Long, commentIds: List<Long>)

    suspend fun removeTypes(commentId: Long, typeIds: List<Long>)

    suspend fun removeAll(commentId: Long)

    suspend fun removeAllByType(typeId: Long)

    suspend fun clear()
}