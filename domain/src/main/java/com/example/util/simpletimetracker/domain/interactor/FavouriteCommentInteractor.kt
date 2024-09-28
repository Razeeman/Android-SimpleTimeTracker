package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.model.FavouriteComment
import com.example.util.simpletimetracker.domain.repo.FavouriteCommentRepo
import java.util.Locale
import javax.inject.Inject

class FavouriteCommentInteractor @Inject constructor(
    private val repo: FavouriteCommentRepo,
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
    }

    fun sort(
        data: List<FavouriteComment>,
    ): List<FavouriteComment> {
        return data.sortedBy { it.comment.lowercase(Locale.getDefault()) }
    }
}