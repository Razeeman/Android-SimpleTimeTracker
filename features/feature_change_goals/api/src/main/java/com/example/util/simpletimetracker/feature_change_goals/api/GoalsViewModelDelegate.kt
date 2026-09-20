package com.example.util.simpletimetracker.feature_change_goals.api

import androidx.lifecycle.LiveData
import com.example.util.simpletimetracker.feature_base_adapter.buttonsRow.view.ButtonsRowViewData
import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal
import com.example.util.simpletimetracker.feature_base_adapter.dayOfWeek.DayOfWeekViewData

interface GoalsViewModelDelegate {
    val goalsViewData: LiveData<ChangeRecordTypeGoalsViewData>

    suspend fun initialize(id: RecordTypeGoal.IdData)
    fun onGoalsVisible()
    fun onNotificationsHintClick()
    fun onGoalDurationSet(tag: String?, duration: Long, anchor: Any)
    fun onGoalDurationDisabled(tag: String?)
    fun onGoalAdd()
    fun onGoalScrollHandled(key: Long)
    fun onGoalRemove(key: Long)
    fun onGoalRangeSelected(key: Long, position: Int)
    fun onGoalTypeSelected(key: Long, position: Int)
    fun onGoalSubTypeSelected(key: Long, viewData: ButtonsRowViewData)
    fun onGoalCountChange(key: Long, count: String)
    fun onGoalTimeClick(key: Long)
    fun onDayOfWeekClick(key: Long, data: DayOfWeekViewData)
    suspend fun saveGoals(id: RecordTypeGoal.IdData): List<Long>
}