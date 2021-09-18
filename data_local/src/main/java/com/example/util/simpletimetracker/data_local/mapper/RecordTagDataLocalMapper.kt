package com.example.util.simpletimetracker.data_local.mapper

import com.example.util.simpletimetracker.data_local.model.RecordTagDBO
import com.example.util.simpletimetracker.domain.model.RecordTag
import javax.inject.Inject

class RecordTagDataLocalMapper @Inject constructor() {

    fun map(dbo: RecordTagDBO): RecordTag {
        return RecordTag(
            id = dbo.id,
            typeId = dbo.typeId,
            name = dbo.name,
            color = dbo.color,
            archived = dbo.archived
        )
    }

    fun map(domain: RecordTag): RecordTagDBO {
        return RecordTagDBO(
            id = domain.id,
            typeId = domain.typeId,
            name = domain.name,
            color = domain.color,
            archived = domain.archived
        )
    }
}