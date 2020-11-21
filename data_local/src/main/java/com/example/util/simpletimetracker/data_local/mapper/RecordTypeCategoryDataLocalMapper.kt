package com.example.util.simpletimetracker.data_local.mapper

import com.example.util.simpletimetracker.data_local.model.RecordTypeCategoryDBO
import javax.inject.Inject

class RecordTypeCategoryDataLocalMapper @Inject constructor() {

    fun map(typeId: Long, categoryId: Long): RecordTypeCategoryDBO {
        return RecordTypeCategoryDBO(
            recordTypeId = typeId,
            categoryId = categoryId
        )
    }
}