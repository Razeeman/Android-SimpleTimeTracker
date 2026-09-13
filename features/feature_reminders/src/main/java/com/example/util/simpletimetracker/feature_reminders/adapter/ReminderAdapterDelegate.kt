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
        tvReminderTitle.text = item.title
        tvReminderSubtitle.text = item.subtitle
        tvReminderSummary.setTextOptional(item.summary)
        containerReminder.setCardBackgroundColor(item.backgroundColor)

        btnReminderEnabled.isVisible = item.button != null
        item.button?.let {
            btnReminderEnabled.setCardBackgroundColor(it.enabledButtonColor)
            tvReminderEnabled.text = it.enabledButtonText
        }

        cardReminderIcon.isVisible = item.icon != null
        item.icon?.let {
            cardReminderIcon.setCardBackgroundColor(item.iconBackgroundColor)
            iconReminder.itemIcon = it
            iconReminder.itemIconColor = item.iconColor
        }

        containerReminder.setOnClickWith(item, onItemClick)
        btnReminderEnabled.setOnClickWith(item, onEnabledClick)
    }
}
