package com.example.util.simpletimetracker.feature_base_adapter.statisticsGoal

import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon

data class StatisticsGoalViewData(
    val id: Long,
    val name: String,
    @ColorInt val color: Int,
    val icon: RecordTypeIcon,
    val goal: Goal,
) : ViewHolderType {

    override fun getUniqueId(): Long = id

    override fun isValidType(other: ViewHolderType): Boolean = other is StatisticsGoalViewData

    data class Goal(
        val goalTime: String,
        val goalPercent: String,
        val goalComplete: Boolean,
        val percent: Long,
    ) {

        companion object {
            fun empty(): Goal = Goal(
                goalTime = "",
                goalPercent = "",
                goalComplete = false,
                percent = 0,
            )
        }
    }
}