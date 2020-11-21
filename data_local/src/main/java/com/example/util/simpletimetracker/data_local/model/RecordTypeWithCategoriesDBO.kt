package com.example.util.simpletimetracker.data_local.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class RecordTypeWithCategoriesDBO(
    @Embedded
    val recordType: RecordTypeDBO,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        entity = CategoryDBO::class,
        associateBy = Junction(
            RecordTypeCategoryDBO::class,
            parentColumn = "record_type_id",
            entityColumn = "category_id"
        )
    )
    val categories: List<CategoryDBO>
)