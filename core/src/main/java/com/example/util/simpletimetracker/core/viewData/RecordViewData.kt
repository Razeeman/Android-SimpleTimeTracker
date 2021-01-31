package com.example.util.simpletimetracker.core.viewData

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.example.util.simpletimetracker.core.adapter.ViewHolderType

sealed class RecordViewData : ViewHolderType {

    abstract val name: String
    abstract val timeStarted: String
    abstract val timeFinished: String
    abstract val duration: String
    abstract val iconId: Int
    abstract val color: Int
    abstract val comment: String

    override fun getViewType(): Int = ViewHolderType.RECORD

    data class Tracked(
        val id: Long,
        override val name: String,
        override val timeStarted: String,
        override val timeFinished: String,
        override val duration: String,
        @DrawableRes override val iconId: Int,
        @ColorInt override val color: Int,
        override val comment: String
    ) : RecordViewData() {

        override fun getUniqueId(): Long? = id
    }

    data class Untracked(
        val timeStartedTimestamp: Long,
        val timeEndedTimestamp: Long,
        override val name: String,
        override val timeStarted: String,
        override val timeFinished: String,
        override val duration: String,
        @DrawableRes override val iconId: Int,
        @ColorInt override val color: Int,
        override val comment: String
    ) : RecordViewData() {

        override fun getUniqueId(): Long? {
            var result = 31L
            result = 31L * result + timeStarted.hashCode()
            result = 31L * result + timeFinished.hashCode()
            return result
        }
    }
}