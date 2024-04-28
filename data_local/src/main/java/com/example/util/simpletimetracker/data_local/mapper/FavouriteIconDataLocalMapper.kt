package com.example.util.simpletimetracker.data_local.mapper

import com.example.util.simpletimetracker.data_local.model.FavouriteIconDBO
import com.example.util.simpletimetracker.domain.model.FavouriteIcon
import javax.inject.Inject

class FavouriteIconDataLocalMapper @Inject constructor() {

    fun map(dbo: FavouriteIconDBO): FavouriteIcon {
        return FavouriteIcon(
            id = dbo.id,
            icon = dbo.icon,
        )
    }

    fun map(domain: FavouriteIcon): FavouriteIconDBO {
        return FavouriteIconDBO(
            id = domain.id,
            icon = domain.icon,
        )
    }
}