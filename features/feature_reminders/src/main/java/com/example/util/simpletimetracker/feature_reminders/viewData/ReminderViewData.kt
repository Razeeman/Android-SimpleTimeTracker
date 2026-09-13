package com.example.util.simpletimetracker.feature_reminders.viewData

import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon

data class ReminderViewData(
    val id: Long,
    val type: Type,
    val title: String,
    val subtitle: CharSequence,
    val summary: CharSequence,
    val enabled: Boolean,
    @ColorInt val backgroundColor: Int,
    val icon: RecordTypeIcon?,
    @ColorInt val iconBackgroundColor: Int,
    @ColorInt val iconColor: Int,
    val button: Button?,
) : ViewHolderType {

    override fun getUniqueId(): Long = id

    override fun isValidType(other: ViewHolderType): Boolean =
        other is ReminderViewData && other.type == type

    data class Button(
        @ColorInt val enabledButtonColor: Int,
        val enabledButtonText: String,
    )

    sealed interface Type {
        data object ScheduledReminder : Type
        data object ActivityReminder : Type
    }
}
