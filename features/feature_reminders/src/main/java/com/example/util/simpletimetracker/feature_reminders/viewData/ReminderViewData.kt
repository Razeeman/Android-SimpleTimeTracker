package com.example.util.simpletimetracker.feature_reminders.viewData

import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon

data class ReminderViewData(
    val id: Long,
    val text: String,
    val scheduleSummary: String,
    val conditionSummary: String,
    val enabled: Boolean,
    @ColorInt val backgroundColor: Int,
    @ColorInt val enabledButtonColor: Int,
    val enabledButtonText: String,
    val activityIcon: RecordTypeIcon?,
    @ColorInt val activityColor: Int,
    @ColorInt val activityIconColor: Int,
) : ViewHolderType {

    override fun getUniqueId(): Long = id

    override fun isValidType(other: ViewHolderType): Boolean = other is ReminderViewData
}
