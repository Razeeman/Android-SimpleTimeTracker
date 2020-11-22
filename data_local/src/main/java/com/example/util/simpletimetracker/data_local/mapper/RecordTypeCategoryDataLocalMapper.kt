package com.example.util.simpletimetracker.data_local.mapper

import com.example.util.simpletimetracker.data_local.model.RecordTypeCategoryDBO
import com.example.util.simpletimetracker.domain.model.RecordTypeCategory
import javax.inject.Inject

class RecordTypeCategoryDataLocalMapper @Inject constructor() {

    fun map(dbo: RecordTypeCategoryDBO): RecordTypeCategory {
        return RecordTypeCategory(
            recordTypeId = dbo.recordTypeId,
            categoryId = dbo.categoryId
        )
    }

    fun map(typeId: Long, categoryId: Long): RecordTypeCategoryDBO {
        return RecordTypeCategoryDBO(
            recordTypeId = typeId,
            categoryId = categoryId
        )
    }
}