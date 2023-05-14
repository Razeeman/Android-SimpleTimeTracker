package com.example.util.simpletimetracker.feature_records_filter.adapter

import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.domain.model.DayOfWeek
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType

data class RecordsFilterDayOfWeekViewData(
    val dayOfWeek: DayOfWeek,
    val text: String,
    @ColorInt val color: Int,
) : ViewHolderType {

    override fun getUniqueId(): Long = dayOfWeek.ordinal.toLong()

    override fun isValidType(other: ViewHolderType): Boolean =
        other is RecordsFilterDayOfWeekViewData
}