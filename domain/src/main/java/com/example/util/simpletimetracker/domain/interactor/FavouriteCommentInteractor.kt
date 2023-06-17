package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.model.FavouriteComment
import com.example.util.simpletimetracker.domain.repo.FavouriteCommentRepo
import java.util.Locale
import javax.inject.Inject

class FavouriteCommentInteractor @Inject constructor(
    private val favouriteCommentRepo: FavouriteCommentRepo,
) {

    suspend fun getAll(): List<FavouriteComment> {
        return favouriteCommentRepo.getAll()
    }

    suspend fun get(id: Long): FavouriteComment? {
        return favouriteCommentRepo.get(id)
    }

    suspend fun get(text: String): FavouriteComment? {
        return favouriteCommentRepo.get(text)
    }

    suspend fun add(comment: FavouriteComment): Long {
        return favouriteCommentRepo.add(comment)
    }

    suspend fun remove(id: Long) {
        favouriteCommentRepo.remove(id)
    }

    private fun sortByName(favouriteComment: List<FavouriteComment>): List<FavouriteComment> {
        return favouriteComment
    }
}