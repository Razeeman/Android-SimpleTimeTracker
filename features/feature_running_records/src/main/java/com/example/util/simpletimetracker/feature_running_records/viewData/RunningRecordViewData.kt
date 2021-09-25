package com.example.util.simpletimetracker.feature_running_records.viewData

import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon

data class RunningRecordViewData(
    val id: Long,
    val name: String,
    val tagName: String,
    val timeStarted: String,
    val timer: String,
    val goalTime: String,
    val iconId: RecordTypeIcon,
    @ColorInt val color: Int,
    val comment: String
) : ViewHolderType {

    override fun getUniqueId(): Long = id

    override fun isValidType(other: ViewHolderType): Boolean = other is RunningRecordViewData

    override fun getChangePayload(other: ViewHolderType): Any? {
        other as RunningRecordViewData
        val updates: MutableList<Int> = mutableListOf()
        if (this.name != other.name) updates.add(UPDATE_NAME)
        if (this.tagName != other.tagName) updates.add(UPDATE_TAG_NAME)
        if (this.timeStarted != other.timeStarted) updates.add(UPDATE_TIME_STARTED)
        if (this.timer != other.timer) updates.add(UPDATE_TIMER)
        if (this.iconId != other.iconId) updates.add(UPDATE_ICON)
        if (this.color != other.color) updates.add(UPDATE_COLOR)
        if (this.goalTime != other.goalTime) updates.add(UPDATE_GOAL_TIME)
        if (this.comment != other.comment) updates.add(UPDATE_COMMENT)

        return updates.takeIf { it.isNotEmpty() }
    }

    companion object {
        const val UPDATE_NAME = 1
        const val UPDATE_TIME_STARTED = 2
        const val UPDATE_TIMER = 3
        const val UPDATE_ICON = 4
        const val UPDATE_COLOR = 5
        const val UPDATE_GOAL_TIME = 6
        const val UPDATE_COMMENT = 7
        const val UPDATE_TAG_NAME = 8
    }
}