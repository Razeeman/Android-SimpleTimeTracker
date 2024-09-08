package com.example.util.simpletimetracker.feature_change_goals.api

import androidx.lifecycle.LiveData
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal
import com.example.util.simpletimetracker.feature_base_adapter.dayOfWeek.DayOfWeekViewData

interface GoalsViewModelDelegate {
    val goalsViewData: LiveData<ChangeRecordTypeGoalsViewData>
    val notificationsHintVisible: LiveData<Boolean>

    suspend fun initialize(id: RecordTypeGoal.IdData)
    fun onGoalsVisible()
    fun onNotificationsHintClick()
    fun onGoalDurationSet(tag: String?, duration: Long, anchor: Any)
    fun onDurationDisabled(tag: String?)
    fun onGoalTypeSelected(range: RecordTypeGoal.Range, position: Int)
    fun onGoalCountChange(range: RecordTypeGoal.Range, count: String)
    fun onGoalTimeClick(range: RecordTypeGoal.Range)
    fun onDayOfWeekClick(data: DayOfWeekViewData)
    suspend fun saveGoals(id: RecordTypeGoal.IdData)
}