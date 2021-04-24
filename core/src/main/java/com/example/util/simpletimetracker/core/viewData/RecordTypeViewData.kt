package com.example.util.simpletimetracker.core.viewData

import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.core.adapter.ViewHolderType

data class RecordTypeViewData(
    val id: Long,
    val name: String,
    val iconId: RecordTypeIcon,
    @ColorInt val iconColor: Int,
    val iconAlpha: Float = 1.0f,
    @ColorInt val color: Int,
    val width: Int? = null,
    val height: Int? = null,
    val asRow: Boolean = false
) : ViewHolderType {

    override fun getUniqueId(): Long = id

    override fun isValidType(other: ViewHolderType): Boolean = other is RecordTypeViewData
}