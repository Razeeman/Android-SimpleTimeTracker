package com.example.util.simpletimetracker.feature_base_adapter.dayOfWeek

import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.domain.model.DayOfWeek
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType

data class DayOfWeekViewData(
    val dayOfWeek: DayOfWeek,
    val text: String,
    @ColorInt val color: Int,
    val width: Width,
    val paddingHorizontalDp: Int,
) : ViewHolderType {

    override fun getUniqueId(): Long = dayOfWeek.ordinal.toLong()

    override fun isValidType(other: ViewHolderType): Boolean =
        other is DayOfWeekViewData

    sealed interface Width {
        object MatchParent : Width
        object WrapContent : Width
    }
}