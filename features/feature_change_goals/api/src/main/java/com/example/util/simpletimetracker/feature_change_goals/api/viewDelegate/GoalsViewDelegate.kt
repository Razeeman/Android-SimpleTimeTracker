package com.example.util.simpletimetracker.feature_change_goals.api.viewDelegate

import com.example.util.simpletimetracker.feature_change_goals.api.ChangeRecordTypeGoalsViewData

interface GoalsViewDelegate {

    fun initUi()

    fun onResume()

    fun updateGoalsState(state: ChangeRecordTypeGoalsViewData)
}
