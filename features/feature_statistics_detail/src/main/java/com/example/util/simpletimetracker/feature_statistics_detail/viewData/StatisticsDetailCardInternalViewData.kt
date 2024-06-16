package com.example.util.simpletimetracker.feature_statistics_detail.viewData

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType

data class StatisticsDetailCardInternalViewData(
    val value: String,
    val valueChange: ValueChange,
    val secondValue: String,
    val description: String,
    val icon: Icon? = null,
    val clickable: ClickableType? = null,
    val accented: Boolean = false,
    val titleTextSizeSp: Int = 16,
    val subtitleTextSizeSp: Int = 14,
) : ViewHolderType {

    override fun getUniqueId(): Long = description.hashCode().toLong()

    override fun isValidType(other: ViewHolderType): Boolean = other is StatisticsDetailCardInternalViewData

    data class Icon(
        @DrawableRes val iconDrawable: Int,
        @ColorInt val iconColor: Int,
    )

    sealed interface ValueChange {
        object None : ValueChange
        data class Present(
            val text: String,
            @ColorInt val color: Int,
        ) : ValueChange
    }

    interface ClickableType
}