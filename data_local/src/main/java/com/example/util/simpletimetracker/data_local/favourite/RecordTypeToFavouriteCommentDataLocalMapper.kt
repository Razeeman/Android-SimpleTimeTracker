package com.example.util.simpletimetracker.data_local.favourite

import com.example.util.simpletimetracker.domain.favourite.model.RecordTypeToFavouriteComment
import javax.inject.Inject

class RecordTypeToFavouriteCommentDataLocalMapper @Inject constructor() {

    fun map(dbo: RecordTypeToFavouriteCommentDBO): RecordTypeToFavouriteComment {
        return RecordTypeToFavouriteComment(
            recordTypeId = dbo.recordTypeId,
            commentId = dbo.commentId,
        )
    }

    fun map(typeId: Long, commentId: Long): RecordTypeToFavouriteCommentDBO {
        return RecordTypeToFavouriteCommentDBO(
            recordTypeId = typeId,
            commentId = commentId,
        )
    }

    fun map(domain: RecordTypeToFavouriteComment): RecordTypeToFavouriteCommentDBO {
        return RecordTypeToFavouriteCommentDBO(
            recordTypeId = domain.recordTypeId,
            commentId = domain.commentId,
        )
    }
}