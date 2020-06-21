package com.example.util.simpletimetracker.feature_running_records.viewData

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.example.util.simpletimetracker.core.adapter.ViewHolderType

data class RunningRecordViewData(
    val id: Long,
    val name: String,
    val timeStarted: String,
    val timer: String,
    @DrawableRes val iconId: Int,
    @ColorInt val color: Int
) : ViewHolderType {

    override fun getViewType(): Int = ViewHolderType.RUNNING_RECORD

    override fun getUniqueId(): Long? = id

    override fun getChangePayload(other: ViewHolderType): Any? {
        other as RunningRecordViewData
        val updates: MutableList<Int> = mutableListOf()
        if (this.name != other.name) updates.add(UPDATE_NAME)
        if (this.timeStarted != other.timeStarted) updates.add(UPDATE_TIME_STARTED)
        if (this.timer != other.timer) updates.add(UPDATE_TIMER)
        if (this.iconId != other.iconId) updates.add(UPDATE_ICON)
        if (this.color != other.color) updates.add(UPDATE_COLOR)

        return updates.takeIf { it.isNotEmpty() }
    }

    companion object {
        const val UPDATE_NAME = 1
        const val UPDATE_TIME_STARTED = 2
        const val UPDATE_TIMER = 3
        const val UPDATE_ICON = 4
        const val UPDATE_COLOR = 5
    }
}