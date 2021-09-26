package com.example.util.simpletimetracker.core.extension

import android.os.StrictMode
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.navigation.params.screen.TypesFilterParams
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
    return (
        tagId != 0L && tagId !in filter.filteredRecordTags
            .filterIsInstance<TypesFilterParams.FilteredRecordTag.Tagged>().map { it.id }
        ) || (
        tagId == 0L && typeId !in filter.filteredRecordTags
            .filterIsInstance<TypesFilterParams.FilteredRecordTag.Untagged>().map { it.typeId }
        )
}

fun Calendar.setWeekToFirstDay() {
    val another = Calendar.getInstance()
    another.timeInMillis = timeInMillis

    val currentTime = another.timeInMillis
    // Setting DAY_OF_WEEK have a weird behaviour so as if after that another field is set -
    // it would reset to current day. Use another calendar to manipulate day of week and get its time.
    another.set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
    // If went to future - go back a week
    if (another.timeInMillis > currentTime) {
        another.add(Calendar.DATE, -7)
    }

    timeInMillis = another.timeInMillis
}