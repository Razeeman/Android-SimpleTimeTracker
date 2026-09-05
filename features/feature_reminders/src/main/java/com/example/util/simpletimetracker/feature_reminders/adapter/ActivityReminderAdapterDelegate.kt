package com.example.util.simpletimetracker.feature_reminders.adapter

import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setOnClickWith
import com.example.util.simpletimetracker.feature_reminders.databinding.ItemActivityReminderLayoutBinding as Binding
import com.example.util.simpletimetracker.feature_reminders.viewData.ActivityReminderViewData as ViewData

fun createActivityReminderAdapterDelegate(
    onItemClick: (ViewData) -> Unit,
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(Binding::inflate) { binding, item, _ ->
    item as ViewData

    with(binding) {
        tvActivityReminderName.text = item.name
        tvActivityReminderMode.text = item.mode
        tvActivityReminderSummary.text = item.summary
        cardActivityReminderIcon.setCardBackgroundColor(item.iconBackgroundColor)
        iconActivityReminder.itemIcon = item.icon
        iconActivityReminder.itemIconColor = item.iconColor
        containerActivityReminder.setCardBackgroundColor(item.backgroundColor)
        containerActivityReminder.setOnClickWith(item, onItemClick)
    }
}
