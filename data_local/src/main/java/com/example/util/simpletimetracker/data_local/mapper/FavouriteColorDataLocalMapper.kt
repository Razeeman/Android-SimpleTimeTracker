package com.example.util.simpletimetracker.data_local.mapper

import com.example.util.simpletimetracker.data_local.model.FavouriteColorDBO
import com.example.util.simpletimetracker.domain.model.FavouriteColor
import javax.inject.Inject

class FavouriteColorDataLocalMapper @Inject constructor() {

    fun map(dbo: FavouriteColorDBO): FavouriteColor {
        return FavouriteColor(
            id = dbo.id,
            colorInt = dbo.colorInt,
        )
    }

    fun map(domain: FavouriteColor): FavouriteColorDBO {
        return FavouriteColorDBO(
            id = domain.id,
            colorInt = domain.colorInt,
        )
    }
}