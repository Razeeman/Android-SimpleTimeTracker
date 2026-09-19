package com.example.util.simpletimetracker.feature_dialogs.typesSelection.model

import com.example.util.simpletimetracker.domain.category.model.Category
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTag
import com.example.util.simpletimetracker.domain.recordType.model.RecordType

sealed interface TypesSelectionCacheHolder {
    val id: Long

    data class Type(val data: RecordType) : TypesSelectionCacheHolder {
        override val id: Long get() = data.id
    }

    data class CategoryItem(val data: Category) : TypesSelectionCacheHolder {
        override val id: Long get() = data.id
    }

    data class Tag(val data: RecordTag) : TypesSelectionCacheHolder {
        override val id: Long get() = data.id
    }
}