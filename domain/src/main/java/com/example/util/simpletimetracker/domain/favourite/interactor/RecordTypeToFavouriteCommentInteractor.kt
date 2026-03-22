package com.example.util.simpletimetracker.domain.favourite.interactor

import com.example.util.simpletimetracker.domain.favourite.model.FavouriteComment
import com.example.util.simpletimetracker.domain.favourite.model.RecordTypeToFavouriteComment
import com.example.util.simpletimetracker.domain.favourite.repo.RecordTypeToFavouriteCommentRepo
import javax.inject.Inject

class RecordTypeToFavouriteCommentInteractor @Inject constructor(
    private val repo: RecordTypeToFavouriteCommentRepo,
) {

    suspend fun getAll(): List<RecordTypeToFavouriteComment> {
        return repo.getAll()
    }

    suspend fun getComments(typeId: Long): Set<Long> {
        return repo.getCommentIdsByType(typeId)
    }

    suspend fun getTypes(commentId: Long): Set<Long> {
        return repo.getTypeIdsByComment(commentId)
    }

    suspend fun addTypes(commentId: Long, typeIds: List<Long>) {
        repo.addTypes(commentId, typeIds)
    }

    suspend fun addComments(typeId: Long, commentIds: List<Long>) {
        repo.addComments(typeId, commentIds)
    }

    suspend fun removeTypes(commentId: Long, typeIds: List<Long>) {
        repo.removeTypes(commentId, typeIds)
    }

    suspend fun filterFavourites(typeId: Long, comments: List<FavouriteComment>): List<FavouriteComment> {
        val (included, excluded) = getAll().partition { it.recordTypeId == typeId }
        val includedIds = included.map { it.commentId }
        val excludedIds = excluded.map { it.commentId }
        return comments.filter { includedIds.contains(it.id) || !excludedIds.contains(it.id) }
    }
}