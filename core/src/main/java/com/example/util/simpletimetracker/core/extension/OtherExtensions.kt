package com.example.util.simpletimetracker.core.extension

import android.os.StrictMode
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.navigation.params.TypesFilterParams

inline fun <T, R> T.allowDiskWrite(block: T.() -> R): R {
    val oldPolicy = StrictMode.allowThreadDiskWrites()
    try {
        return block()
    } finally {
        StrictMode.setThreadPolicy(oldPolicy)
    }
}

fun Record.isNotFiltered(filter: TypesFilterParams): Boolean {
    return (tagId != 0L && tagId !in filter.filteredRecordTags
        .filterIsInstance<TypesFilterParams.FilteredRecordTag.Tagged>().map { it.id }) ||
        (tagId == 0L && typeId !in filter.filteredRecordTags
            .filterIsInstance<TypesFilterParams.FilteredRecordTag.Untagged>().map { it.typeId })
}