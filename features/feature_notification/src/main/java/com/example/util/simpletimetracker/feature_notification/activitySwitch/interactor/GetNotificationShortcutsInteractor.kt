package com.example.util.simpletimetracker.feature_notification.activitySwitch.interactor

import com.example.util.simpletimetracker.core.interactor.IsSettingShortcutEnabledInteractor
import com.example.util.simpletimetracker.core.mapper.RecordShortcutViewDataMapper
import com.example.util.simpletimetracker.domain.recordShortcut.interactor.RecordShortcutInteractor
import com.example.util.simpletimetracker.domain.recordShortcut.model.RecordShortcut
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTag
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.feature_notification.R
import com.example.util.simpletimetracker.feature_notification.activitySwitch.manager.NotificationControlsParams
import javax.inject.Inject

class GetNotificationShortcutsInteractor @Inject constructor(
    private val recordShortcutInteractor: RecordShortcutInteractor,
    private val recordShortcutViewDataMapper: RecordShortcutViewDataMapper,
    private val isSettingShortcutEnabledInteractor: IsSettingShortcutEnabledInteractor,
) {

    suspend fun execute(
        typesMap: Map<Long, RecordType>,
        tags: List<RecordTag>,
        isDarkTheme: Boolean,
    ): List<NotificationControlsParams.Shortcut> {
        return recordShortcutInteractor.getAll()
            .filter { it.target.isSupportedInNotification() }
            .map { shortcut ->
                val isEnabled = when (val target = shortcut.target) {
                    is RecordShortcut.Target.Record -> false
                    is RecordShortcut.Target.Setting -> isSettingShortcutEnabledInteractor.execute(target)
                }
                val viewData = recordShortcutViewDataMapper.map(
                    shortcut = shortcut,
                    typesMap = typesMap,
                    tags = tags,
                    isDarkTheme = isDarkTheme,
                    isFiltered = false,
                    isEnabled = isEnabled,
                    spinnerData = null,
                )
                NotificationControlsParams.Shortcut(
                    id = viewData.id,
                    text = viewData.data.name,
                    color = viewData.data.color,
                    iconResId = when (shortcut.target) {
                        is RecordShortcut.Target.Record -> R.drawable.ic_running_record_now
                        is RecordShortcut.Target.Setting -> R.drawable.ic_settings
                    },
                )
            }
    }

    private fun RecordShortcut.Target.isSupportedInNotification(): Boolean {
        return when (this) {
            is RecordShortcut.Target.Record -> true
            is RecordShortcut.Target.Setting -> action is RecordShortcut.SettingAction.Toggle
        }
    }
}
