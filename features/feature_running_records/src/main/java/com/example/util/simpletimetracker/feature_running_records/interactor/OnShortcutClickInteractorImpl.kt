package com.example.util.simpletimetracker.feature_running_records.interactor

import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.recordAction.interactor.RecordActionRepeatMediator
import com.example.util.simpletimetracker.domain.recordShortcut.interactor.RecordShortcutInteractor
import com.example.util.simpletimetracker.domain.recordShortcut.model.RecordShortcut
import com.example.util.simpletimetracker.domain.recordType.model.CardOrder
import com.example.util.simpletimetracker.feature_base_adapter.recordShortcut.RecordShortcutViewData
import com.example.util.simpletimetracker.feature_running_records.api.OnShortcutClickInteractor
import com.example.util.simpletimetracker.feature_settings.api.OnSettingChangedInteractor
import com.example.util.simpletimetracker.feature_settings.api.SettingsBlock
import com.example.util.simpletimetracker.feature_settings.api.SettingsCardOrderMapper
import com.example.util.simpletimetracker.feature_settings.api.SettingsOrderChangeInteractor
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.ArchiveParams
import com.example.util.simpletimetracker.navigation.params.screen.CardOrderDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.CategoriesParams
import com.example.util.simpletimetracker.navigation.params.screen.DataEditParams
import com.example.util.simpletimetracker.navigation.params.screen.RemindersParams
import com.example.util.simpletimetracker.navigation.params.screen.ShortcutsParams
import javax.inject.Inject

class OnShortcutClickInteractorImpl @Inject constructor(
    private val router: Router,
    private val prefsInteractor: PrefsInteractor,
    private val recordActionRepeatMediator: RecordActionRepeatMediator,
    private val recordShortcutInteractor: RecordShortcutInteractor,
    private val onSettingChangedInteractor: OnSettingChangedInteractor,
    private val settingsCardOrderMapper: SettingsCardOrderMapper,
    private val settingsOrderChangeInteractor: SettingsOrderChangeInteractor,
) : OnShortcutClickInteractor {

    override suspend fun execute(data: RecordShortcutViewData): OnShortcutClickInteractor.ExecuteResult {
        val shortcut = recordShortcutInteractor.get(data.id)
            ?: return OnShortcutClickInteractor.ExecuteResult.DataChanged
        return when (val target = shortcut.target) {
            is RecordShortcut.Target.Record -> {
                executeRecordAction(target)
                OnShortcutClickInteractor.ExecuteResult.DataChanged
            }
            is RecordShortcut.Target.Setting -> executeSettingAction(target)
        }
    }

    override suspend fun onSpinnerPositionSelected(data: RecordShortcutViewData, position: Int) {
        when (data.spinnerData?.block) {
            SettingsBlock.DisplaySortActivities -> {
                val order = settingsCardOrderMapper.toCardOrder(position)
                val type = CardOrderDialogParams.Type.RecordType(order)
                settingsOrderChangeInteractor.onOrderSelected(type)
            }
        }
    }

    override fun onButtonClick(data: RecordShortcutViewData) {
        when (data.spinnerData?.block) {
            SettingsBlock.DisplaySortActivities -> {
                val order = CardOrder.MANUAL
                val type = CardOrderDialogParams.Type.RecordType(order)
                settingsOrderChangeInteractor.openOrderDialog(type)
            }
        }
    }

    private suspend fun executeRecordAction(target: RecordShortcut.Target.Record) {
        recordActionRepeatMediator.execute(
            typeId = target.typeId,
            comment = target.comment,
            tags = target.tags,
        )
    }

    private suspend fun executeSettingAction(
        target: RecordShortcut.Target.Setting,
    ): OnShortcutClickInteractor.ExecuteResult {
        val action = target.action
        return when (action) {
            RecordShortcut.SettingAction.Multitasking -> {
                val newValue = !prefsInteractor.getAllowMultitasking()
                prefsInteractor.setAllowMultitasking(newValue)
                onSettingChangedInteractor.onAllowMultitaskingChange()
                OnShortcutClickInteractor.ExecuteResult.DataChanged
            }
            RecordShortcut.SettingAction.RetroactiveMode -> {
                val newValue = !prefsInteractor.getRetroactiveTrackingMode()
                prefsInteractor.setRetroactiveTrackingMode(newValue)
                onSettingChangedInteractor.onRetroactiveTrackingModeChange()
                OnShortcutClickInteractor.ExecuteResult.DataChanged
            }
            RecordShortcut.SettingAction.Categories -> {
                router.navigate(CategoriesParams)
                OnShortcutClickInteractor.ExecuteResult.Navigation
            }
            RecordShortcut.SettingAction.Archive -> {
                router.navigate(ArchiveParams)
                OnShortcutClickInteractor.ExecuteResult.Navigation
            }
            RecordShortcut.SettingAction.DataEdit -> {
                router.navigate(DataEditParams)
                OnShortcutClickInteractor.ExecuteResult.Navigation
            }
            RecordShortcut.SettingAction.SortActivities -> {
                // Not used, click on spinner should work instead.
                OnShortcutClickInteractor.ExecuteResult.DataChanged
            }
            RecordShortcut.SettingAction.Shortcuts -> {
                router.navigate(ShortcutsParams)
                OnShortcutClickInteractor.ExecuteResult.Navigation
            }
            RecordShortcut.SettingAction.Reminders -> {
                router.navigate(RemindersParams)
                OnShortcutClickInteractor.ExecuteResult.Navigation
            }
        }
    }
}