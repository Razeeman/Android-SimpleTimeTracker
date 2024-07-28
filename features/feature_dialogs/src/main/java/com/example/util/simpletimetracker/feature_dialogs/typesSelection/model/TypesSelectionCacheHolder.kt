package com.example.util.simpletimetracker.feature_dialogs.typesSelection.model

import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RecordType

sealed interface TypesSelectionCacheHolder {
    val id: Long

    data class Type(val data: RecordType) : TypesSelectionCacheHolder {
        override val id: Long get() = data.id
    }

    data class Tag(val data: RecordTag) : TypesSelectionCacheHolder {
        override val id: Long get() = data.id
    }
}