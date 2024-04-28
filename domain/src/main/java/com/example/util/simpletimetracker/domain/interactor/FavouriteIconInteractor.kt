package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.model.FavouriteIcon
import com.example.util.simpletimetracker.domain.repo.FavouriteIconRepo
import javax.inject.Inject

class FavouriteIconInteractor @Inject constructor(
    private val repo: FavouriteIconRepo,
) {

    suspend fun getAll(): List<FavouriteIcon> {
        return repo.getAll()
    }

    suspend fun get(icon: String): FavouriteIcon? {
        return repo.get(icon)
    }

    suspend fun add(icon: FavouriteIcon): Long {
        return repo.add(icon)
    }

    suspend fun remove(id: Long) {
        repo.remove(id)
    }
}