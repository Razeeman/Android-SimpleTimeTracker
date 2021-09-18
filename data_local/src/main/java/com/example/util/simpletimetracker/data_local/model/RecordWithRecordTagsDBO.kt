package com.example.util.simpletimetracker.data_local.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class RecordWithRecordTagsDBO(
    @Embedded
    val record: RecordDBO,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        entity = RecordTagDBO::class,
        associateBy = Junction(
            RecordToRecordTagDBO::class,
            parentColumn = "record_id",
            entityColumn = "record_tag_id"
        )
    )
    val recordTags: List<RecordTagDBO>
)