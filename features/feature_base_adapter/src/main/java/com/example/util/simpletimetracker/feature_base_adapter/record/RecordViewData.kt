package com.example.util.simpletimetracker.feature_base_adapter.record

import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon

sealed class RecordViewData : ViewHolderType {

    abstract val timeStartedTimestamp: Long
    abstract val timeEndedTimestamp: Long
    abstract val name: String
    abstract val tagName: String
    abstract val timeStarted: String
    abstract val timeFinished: String
    abstract val duration: String
    abstract val iconId: RecordTypeIcon
    abstract val color: Int
    abstract val comment: String

    data class Tracked(
        val id: Long,
        override val timeStartedTimestamp: Long,
        override val timeEndedTimestamp: Long,
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

        override fun isValidType(other: ViewHolderType): Boolean = other is Tracked
    }

    data class Untracked(
        override val timeStartedTimestamp: Long,
        override val timeEndedTimestamp: Long,
        override val name: String,
        override val timeStarted: String,
        override val timeFinished: String,
        override val duration: String,
        override val iconId: RecordTypeIcon,
        @ColorInt override val color: Int,
    ) : RecordViewData() {

        override val tagName: String = ""
        override val comment: String = ""

        override fun getUniqueId(): Long {
            return timeStartedTimestamp.hashCode().toLong()
        }

        override fun isValidType(other: ViewHolderType): Boolean = other is Untracked
    }
}