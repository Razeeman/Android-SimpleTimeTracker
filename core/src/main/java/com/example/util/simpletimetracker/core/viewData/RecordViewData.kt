package com.example.util.simpletimetracker.core.viewData

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.example.util.simpletimetracker.core.adapter.ViewHolderType

sealed class RecordViewData : ViewHolderType {

    abstract var name: String
    abstract var timeStarted: String
    abstract var timeFinished: String
    abstract var duration: String
    abstract val iconId: Int
    abstract val color: Int

    override fun getViewType(): Int = ViewHolderType.RECORD

    data class Tracked(
        var id: Long,
        override var name: String,
        override var timeStarted: String,
        override var timeFinished: String,
        override var duration: String,
        @DrawableRes override val iconId: Int,
        @ColorInt override val color: Int
    ) : RecordViewData() {

        override fun getUniqueId(): Long? = id
    }

    data class Untracked(
        var timeStartedTimestamp: Long,
        var timeEndedTimestamp: Long,
        override var name: String,
        override var timeStarted: String,
        override var timeFinished: String,
        override var duration: String,
        @DrawableRes override val iconId: Int,
        @ColorInt override val color: Int
    ) : RecordViewData() {

        override fun getUniqueId(): Long? {
            var result = 31L
            result = 31L * result + timeStarted.hashCode()
            result = 31L * result + timeFinished.hashCode()
            return result
        }
    }
}