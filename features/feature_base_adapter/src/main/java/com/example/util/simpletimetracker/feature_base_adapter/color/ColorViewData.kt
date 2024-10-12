package com.example.util.simpletimetracker.feature_base_adapter.color

import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType

data class ColorViewData(
    val colorId: Long,
    val type: Type,
    @ColorInt val colorInt: Int,
    val selected: Boolean,
) : ViewHolderType {

    override fun getUniqueId(): Long = colorId

    override fun isValidType(other: ViewHolderType): Boolean =
        other is ColorViewData && other.type == type

    sealed interface Type {
        object Base : Type
        object Favourite : Type
    }
}