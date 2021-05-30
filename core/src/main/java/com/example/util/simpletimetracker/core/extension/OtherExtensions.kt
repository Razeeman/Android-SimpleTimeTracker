package com.example.util.simpletimetracker.core.extension

import android.os.StrictMode
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.navigation.params.TypesFilterParams
import java.util.Calendar

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

fun Calendar.setWeekToFirstDay() {
    val currentTime = timeInMillis
    set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
    // If went to future - go back a week
    if (timeInMillis > currentTime) {
        add(Calendar.DATE, -7)
    }
}