package com.example.util.simpletimetracker.data_local.favourite

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "recordTypeToFavouriteComment", primaryKeys = ["record_type_id", "comment_id"])
data class RecordTypeToFavouriteCommentDBO(
    @ColumnInfo(name = "record_type_id")
    val recordTypeId: Long,
    @ColumnInfo(name = "comment_id")
    val commentId: Long,
)