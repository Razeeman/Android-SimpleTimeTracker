package com.example.util.simpletimetracker.feature_reminders.adapter

import androidx.core.view.isVisible
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setOnClickWith
import com.example.util.simpletimetracker.feature_views.extension.setTextOptional
import com.example.util.simpletimetracker.feature_reminders.databinding.ItemReminderLayoutBinding as Binding
import com.example.util.simpletimetracker.feature_reminders.viewData.ReminderViewData as ViewData

fun createReminderAdapterDelegate(
    onItemClick: (ViewData) -> Unit,
    onEnabledClick: (ViewData) -> Unit,
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->
    item as ViewData

    with(binding) {
        tvReminderText.text = item.text
        tvReminderSchedule.text = item.scheduleSummary
        tvReminderCondition.setTextOptional(item.conditionSummary)
        btnReminderEnabled.setCardBackgroundColor(item.enabledButtonColor)
        tvReminderEnabled.text = item.enabledButtonText

        cardReminderActivityIcon.isVisible = item.activityIcon != null
        item.activityIcon?.let {
            cardReminderActivityIcon.setCardBackgroundColor(item.activityColor)
            iconReminderActivity.itemIcon = it
            iconReminderActivity.itemIconColor = item.activityIconColor
        }

        containerReminder.setOnClickWith(item, onItemClick)
        btnReminderEnabled.setOnClickWith(item, onEnabledClick)
    }
}
