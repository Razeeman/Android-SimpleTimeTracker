package com.example.util.simpletimetracker.feature_settings.viewModel.delegate

import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.domain.extension.flip
import com.example.util.simpletimetracker.domain.interactor.NotificationGoalTimeInteractor
import com.example.util.simpletimetracker.domain.interactor.NotificationTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.WidgetInteractor
import com.example.util.simpletimetracker.domain.model.WidgetType
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_settings.interactor.SettingsAdditionalViewDataInteractor
import com.example.util.simpletimetracker.feature_settings.mapper.SettingsMapper
import com.example.util.simpletimetracker.feature_settings.viewModel.SettingsViewModel
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.DurationDialogParams
import kotlinx.coroutines.launch
import javax.inject.Inject

class SettingsAdditionalViewModelDelegate @Inject constructor(
    private val router: Router,
    private val prefsInteractor: PrefsInteractor,
    private val settingsMapper: SettingsMapper,
    private val notificationTypeInteractor: NotificationTypeInteractor,
    private val widgetInteractor: WidgetInteractor,
    private val notificationGoalTimeInteractor: NotificationGoalTimeInteractor,
    private val settingsAdditionalViewDataInteractor: SettingsAdditionalViewDataInteractor,
) : ViewModelDelegate() {

    private var parent: SettingsParent? = null
    private var isCollapsed: Boolean = true

    fun init(parent: SettingsParent) {
        this.parent = parent
    }

    suspend fun getViewData(): List<ViewHolderType> {
        return settingsAdditionalViewDataInteractor.execute(
            isCollapsed = isCollapsed,
        )
    }

    fun onCollapseClick() = delegateScope.launch {
        isCollapsed = isCollapsed.flip()
        parent?.updateContent()
    }

    fun onFirstDayOfWeekSelected(position: Int) {
        val newDayOfWeek = settingsMapper.toDayOfWeek(position)

        delegateScope.launch {
            prefsInteractor.setFirstDayOfWeek(newDayOfWeek)
            widgetInteractor.updateWidgets(listOf(WidgetType.STATISTICS_CHART))
            notificationGoalTimeInteractor.checkAndReschedule()
            parent?.updateContent()
        }
    }

    fun onRepeatButtonSelected(position: Int) {
        val newType = settingsMapper.toRepeatButtonType(position)

        delegateScope.launch {
            prefsInteractor.setRepeatButtonType(newType)
            parent?.updateContent()
        }
    }

    fun onStartOfDayClicked() {
        delegateScope.launch {
            parent?.openDateTimeDialog(
                tag = SettingsViewModel.START_OF_DAY_DIALOG_TAG,
                timestamp = prefsInteractor.getStartOfDayShift(),
                useMilitaryTime = true,
            )
        }
    }

    fun onStartOfDaySignClicked() {
        delegateScope.launch {
            val newValue = prefsInteractor.getStartOfDayShift() * -1
            prefsInteractor.setStartOfDayShift(newValue)
            widgetInteractor.updateWidgets(listOf(WidgetType.STATISTICS_CHART))
            widgetInteractor.updateWidgets(listOf(WidgetType.RECORD_TYPE))
            notificationTypeInteractor.updateNotifications()
            notificationGoalTimeInteractor.checkAndReschedule()
            parent?.updateContent()
        }
    }

    fun onKeepStatisticsRangeClicked() {
        delegateScope.launch {
            val newValue = !prefsInteractor.getKeepStatisticsRange()
            prefsInteractor.setKeepStatisticsRange(newValue)
            parent?.updateContent()
        }
    }

    fun onIgnoreShortRecordsClicked() {
        delegateScope.launch {
            DurationDialogParams(
                tag = SettingsViewModel.IGNORE_SHORT_RECORDS_DIALOG_TAG,
                duration = prefsInteractor.getIgnoreShortRecordsDuration(),
            ).let(router::navigate)
        }
    }

    fun onShowRecordTagSelectionClicked() {
        delegateScope.launch {
            val newValue = !prefsInteractor.getShowRecordTagSelection()
            prefsInteractor.setShowRecordTagSelection(newValue)
            widgetInteractor.updateWidgets(listOf(WidgetType.QUICK_SETTINGS))
            parent?.updateContent()
        }
    }

    fun onRecordTagSelectionCloseClicked() {
        delegateScope.launch {
            val newValue = !prefsInteractor.getRecordTagSelectionCloseAfterOne()
            prefsInteractor.setRecordTagSelectionCloseAfterOne(newValue)
            parent?.updateContent()
        }
    }

    fun onRecordTagSelectionGeneralClicked() {
        delegateScope.launch {
            val newValue = !prefsInteractor.getRecordTagSelectionEvenForGeneralTags()
            prefsInteractor.setRecordTagSelectionEvenForGeneralTags(newValue)
            parent?.updateContent()
        }
    }

    fun onAutomatedTrackingSendEventsClicked() {
        delegateScope.launch {
            val newValue = !prefsInteractor.getAutomatedTrackingSendEvents()
            prefsInteractor.setAutomatedTrackingSendEvents(newValue)
            parent?.updateContent()
        }
    }

    fun onAutomatedTrackingHelpClick() {
        router.navigate(
            settingsMapper.toAutomatedTrackingHelpDialog(),
        )
    }

    fun onDurationSet(tag: String?, duration: Long) {
        when (tag) {
            SettingsViewModel.IGNORE_SHORT_RECORDS_DIALOG_TAG -> delegateScope.launch {
                prefsInteractor.setIgnoreShortRecordsDuration(duration)
                parent?.updateContent()
            }
        }
    }

    fun onDurationDisabled(tag: String?) {
        when (tag) {
            SettingsViewModel.IGNORE_SHORT_RECORDS_DIALOG_TAG -> delegateScope.launch {
                prefsInteractor.setIgnoreShortRecordsDuration(0)
                parent?.updateContent()
            }
        }
    }

    fun onDateTimeSet(timestamp: Long, tag: String?) = delegateScope.launch {
        when (tag) {
            SettingsViewModel.START_OF_DAY_DIALOG_TAG -> {
                val wasPositive = prefsInteractor.getStartOfDayShift() >= 0
                val newValue = settingsMapper.toStartOfDayShift(timestamp, wasPositive)
                prefsInteractor.setStartOfDayShift(newValue)
                widgetInteractor.updateWidgets(listOf(WidgetType.STATISTICS_CHART))
                widgetInteractor.updateWidgets(listOf(WidgetType.RECORD_TYPE))
                notificationTypeInteractor.updateNotifications()
                notificationGoalTimeInteractor.checkAndReschedule()
                parent?.updateContent()
            }
        }
    }
}