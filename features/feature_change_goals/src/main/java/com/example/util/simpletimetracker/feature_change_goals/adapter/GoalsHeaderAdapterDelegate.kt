package com.example.util.simpletimetracker.feature_change_goals.adapter

import androidx.core.view.isVisible
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_change_goals.adapter.GoalsHeaderViewData as ViewData
import com.example.util.simpletimetracker.feature_change_goals.databinding.ChangeGoalsHeaderItemBinding as Binding

fun createGoalsHeaderAdapterDelegate(
    onNotificationsHintClick: () -> Unit,
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->
    item as ViewData

    // TODO GOAL use common items
    binding.changeGoalNotificationsHint.isVisible = item.notificationsHintVisible
    binding.changeGoalNotificationsHint.setOnActionClick(onNotificationsHintClick)
}

data class GoalsHeaderViewData(
    val notificationsHintVisible: Boolean,
) : ViewHolderType {

    override fun getUniqueId(): Long = 0L

    override fun isValidType(other: ViewHolderType): Boolean = other is ViewData
}