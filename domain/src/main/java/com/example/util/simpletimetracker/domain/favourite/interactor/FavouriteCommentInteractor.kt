package com.example.util.simpletimetracker.domain.favourite.interactor

import com.example.util.simpletimetracker.domain.favourite.repo.FavouriteCommentRepo
import com.example.util.simpletimetracker.domain.favourite.model.FavouriteComment
import com.example.util.simpletimetracker.domain.favourite.repo.RecordTypeToFavouriteCommentRepo
import java.util.Locale
import javax.inject.Inject

class FavouriteCommentInteractor @Inject constructor(
    private val repo: FavouriteCommentRepo,
    private val recordTypeRepo: RecordTypeToFavouriteCommentRepo,
) {

    suspend fun getAll(): List<FavouriteComment> {
        return repo.getAll().let(::sort)
    }

    suspend fun get(text: String): FavouriteComment? {
        return repo.get(text)
    }

    suspend fun add(comment: FavouriteComment): Long {
        return repo.add(comment)
    }

    suspend fun remove(id: Long) {
        repo.remove(id)
        recordTypeRepo.removeAll(id)
    }

    fun sort(
        data: List<FavouriteComment>,
    ): List<FavouriteComment> {
        return data.sortedBy { it.comment.lowercase(Locale.getDefault()) }
    }
}