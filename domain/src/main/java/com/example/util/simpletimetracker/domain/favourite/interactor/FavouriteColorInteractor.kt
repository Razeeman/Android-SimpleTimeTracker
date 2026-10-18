package com.example.util.simpletimetracker.domain.favourite.interactor

import com.example.util.simpletimetracker.domain.recordType.interactor.SortCardsInteractor
import com.example.util.simpletimetracker.domain.color.model.AppColor
import com.example.util.simpletimetracker.domain.favourite.repo.FavouriteColorRepo
import com.example.util.simpletimetracker.domain.favourite.model.FavouriteColor
import javax.inject.Inject

class FavouriteColorInteractor @Inject constructor(
    private val repo: FavouriteColorRepo,
    private val sortCardsInteractor: SortCardsInteractor,
) {

    suspend fun getAll(): List<FavouriteColor> {
        return repo.getAll().let(::sort)
    }

    suspend fun get(text: String): FavouriteColor? {
        return repo.get(text)
    }

    suspend fun add(color: FavouriteColor): Long {
        return repo.add(color)
    }

    suspend fun remove(id: Long) {
        repo.remove(id)
    }

    fun sort(
        data: List<FavouriteColor>,
    ): List<FavouriteColor> {
        return data.map {
            SortCardsInteractor.DataHolder(
                id = it.id,
                name = "", // Doesn't matter here.
                color = AppColor(
                    colorId = 0,
                    colorInt = it.colorInt,
                    gradient = null, // TODO GRADIENT
                ),
                data = it,
            )
        }.let {
            sortCardsInteractor.sortByColor(it)
        }.map {
            it.data
        }
    }
}