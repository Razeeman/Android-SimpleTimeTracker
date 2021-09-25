package com.example.util.simpletimetracker.feature_change_record_type.viewData

import com.example.util.simpletimetracker.domain.model.IconEmojiType
import com.example.util.simpletimetracker.domain.model.IconImageType

sealed class ChangeRecordTypeIconTypeViewData {
    abstract val id: Long

    data class Image(
        val type: IconImageType,
        override val id: Long = type.ordinal.toLong()
    ) : ChangeRecordTypeIconTypeViewData()

    data class Emoji(
        val type: IconEmojiType,
        override val id: Long = type.ordinal.toLong()
    ) : ChangeRecordTypeIconTypeViewData()
}