package com.example.util.simpletimetracker.feature_reminders.viewData

import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon

data class ActivityReminderViewData(
    val activityId: Long,
    val name: String,
    val mode: String,
    val summary: String,
    val icon: RecordTypeIcon,
    @ColorInt val iconBackgroundColor: Int,
    @ColorInt val iconColor: Int,
    @ColorInt val backgroundColor: Int,
) : ViewHolderType {

    override fun getUniqueId(): Long = activityId

    override fun isValidType(other: ViewHolderType): Boolean =
        other is ActivityReminderViewData
}
