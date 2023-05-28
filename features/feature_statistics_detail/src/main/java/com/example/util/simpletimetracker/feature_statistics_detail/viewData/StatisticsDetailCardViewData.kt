package com.example.util.simpletimetracker.feature_statistics_detail.viewData

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType

data class StatisticsDetailCardViewData(
    val value: String,
    val valueChange: ValueChange,
    val secondValue: String,
    val description: String,
    val icon: Icon? = null,
    val accented: Boolean = false,
) : ViewHolderType {

    override fun getUniqueId(): Long = description.hashCode().toLong()

    override fun isValidType(other: ViewHolderType): Boolean = other is StatisticsDetailCardViewData

    data class Icon(
        @DrawableRes val iconDrawable: Int,
        @ColorInt val iconColor: Int
    )

    sealed interface ValueChange {
        object None: ValueChange
        data class Present(
            val text: String,
            @ColorInt val color: Int,
        ): ValueChange
    }
}