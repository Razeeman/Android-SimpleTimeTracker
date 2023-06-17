package com.example.util.simpletimetracker.data_local.mapper

import com.example.util.simpletimetracker.data_local.model.FavouriteCommentDBO
import com.example.util.simpletimetracker.domain.model.FavouriteComment
import javax.inject.Inject

class FavouriteCommentDataLocalMapper @Inject constructor() {

    fun map(dbo: FavouriteCommentDBO): FavouriteComment {
        return FavouriteComment(
            id = dbo.id,
            comment = dbo.comment,
        )
    }

    fun map(domain: FavouriteComment): FavouriteCommentDBO {
        return FavouriteCommentDBO(
            id = domain.id,
            comment = domain.comment,
        )
    }
}