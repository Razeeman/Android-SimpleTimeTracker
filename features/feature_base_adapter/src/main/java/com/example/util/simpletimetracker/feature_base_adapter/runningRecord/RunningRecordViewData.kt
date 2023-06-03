package com.example.util.simpletimetracker.feature_base_adapter.runningRecord

import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon

data class RunningRecordViewData(
    val id: Long,
    val name: String,
    val tagName: String,
    val timeStarted: String,
    val timer: String,
    val timerTotal: String,
    val goalTime: GoalTimeViewData,
    val goalTime2: GoalTimeViewData,
    val goalTime3: GoalTimeViewData,
    val goalTime4: GoalTimeViewData,
    val iconId: RecordTypeIcon,
    @ColorInt val color: Int,
    val comment: String,
    val nowIconVisible: Boolean,
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
        if (this.timerTotal != other.timerTotal) updates.add(UPDATE_TIMER_TOTAL)
        if (this.iconId != other.iconId) updates.add(UPDATE_ICON)
        if (this.color != other.color) updates.add(UPDATE_COLOR)
        if (this.goalTime != other.goalTime) updates.add(UPDATE_GOAL_TIME)
        if (this.goalTime2 != other.goalTime2) updates.add(UPDATE_GOAL_TIME2)
        if (this.goalTime3 != other.goalTime3) updates.add(UPDATE_GOAL_TIME3)
        if (this.goalTime4 != other.goalTime4) updates.add(UPDATE_GOAL_TIME4)
        if (this.comment != other.comment) updates.add(UPDATE_COMMENT)
        if (this.nowIconVisible != other.nowIconVisible) updates.add(UPDATE_NOW_ICON)

        return updates.takeIf { it.isNotEmpty() }
    }

    companion object {
        const val UPDATE_NAME = 1
        const val UPDATE_TIME_STARTED = 2
        const val UPDATE_TIMER = 3
        const val UPDATE_TIMER_TOTAL = 4
        const val UPDATE_ICON = 5
        const val UPDATE_COLOR = 6
        const val UPDATE_GOAL_TIME = 7
        const val UPDATE_GOAL_TIME2 = 8
        const val UPDATE_GOAL_TIME3 = 9
        const val UPDATE_GOAL_TIME4 = 10
        const val UPDATE_COMMENT = 11
        const val UPDATE_TAG_NAME = 12
        const val UPDATE_NOW_ICON = 13
    }
}