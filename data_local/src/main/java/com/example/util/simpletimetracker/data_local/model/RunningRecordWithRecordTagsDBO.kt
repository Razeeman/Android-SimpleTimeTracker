package com.example.util.simpletimetracker.data_local.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class RunningRecordWithRecordTagsDBO(
    @Embedded
    val runningRecord: RunningRecordDBO,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        entity = RecordTagDBO::class,
        associateBy = Junction(
            RunningRecordToRecordTagDBO::class,
            parentColumn = "running_record_id",
            entityColumn = "record_tag_id"
        )
    )
    val recordTags: List<RecordTagDBO>
)