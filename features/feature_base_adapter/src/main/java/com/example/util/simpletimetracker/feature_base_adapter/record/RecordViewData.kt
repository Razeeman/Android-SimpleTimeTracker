package com.example.util.simpletimetracker.feature_base_adapter.record

import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon

sealed class RecordViewData : ViewHolderType {

    abstract val name: String
    abstract val tagName: String
    abstract val timeStarted: String
    abstract val timeFinished: String
    abstract val duration: String
    abstract val iconId: RecordTypeIcon
    abstract val color: Int
    abstract val comment: String

    override fun isValidType(other: ViewHolderType): Boolean = other is RecordViewData

    data class Tracked(
        val id: Long,
        override val name: String,
        override val tagName: String,
        override val timeStarted: String,
        override val timeFinished: String,
        override val duration: String,
        override val iconId: RecordTypeIcon,
        @ColorInt override val color: Int,
        override val comment: String
    ) : RecordViewData() {

        override fun getUniqueId(): Long = id
    }

    data class Untracked(
        val timeStartedTimestamp: Long,
        val timeEndedTimestamp: Long,
        override val name: String,
        override val tagName: String,
        override val timeStarted: String,
        override val timeFinished: String,
        override val duration: String,
        override val iconId: RecordTypeIcon,
        @ColorInt override val color: Int,
        override val comment: String
    ) : RecordViewData() {

        override fun getUniqueId(): Long {
            var result = 31L
            result = 31L * result + timeStarted.hashCode()
            result = 31L * result + timeFinished.hashCode()
            return result
        }
    }
}