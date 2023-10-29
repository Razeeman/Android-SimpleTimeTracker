package com.example.util.simpletimetracker.feature_settings.viewModel.delegate

import androidx.lifecycle.LiveData
import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.core.extension.lazySuspend
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.NotificationGoalTimeInteractor
import com.example.util.simpletimetracker.domain.interactor.NotificationTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.WidgetInteractor
import com.example.util.simpletimetracker.domain.model.WidgetType
import com.example.util.simpletimetracker.feature_settings.R
import com.example.util.simpletimetracker.feature_settings.mapper.SettingsMapper
import com.example.util.simpletimetracker.feature_settings.viewData.FirstDayOfWeekViewData
import com.example.util.simpletimetracker.feature_settings.viewData.RepeatButtonViewData
import com.example.util.simpletimetracker.feature_settings.viewData.SettingsStartOfDayViewData
import com.example.util.simpletimetracker.feature_settings.viewModel.SettingsViewModel
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.DurationDialogParams
import kotlinx.coroutines.launch
import javax.inject.Inject

class SettingsAdditionalViewModelDelegate @Inject constructor(
    private val router: Router,
    private val timeMapper: TimeMapper,
    private val resourceRepo: ResourceRepo,
    private val prefsInteractor: PrefsInteractor,
    private val settingsMapper: SettingsMapper,
    private val notificationTypeInteractor: NotificationTypeInteractor,
    private val widgetInteractor: WidgetInteractor,
    private val notificationGoalTimeInteractor: NotificationGoalTimeInteractor,
) : ViewModelDelegate() {

    val firstDayOfWeekViewData: LiveData<FirstDayOfWeekViewData>
        by lazySuspend { loadFirstDayOfWeekViewData() }
    val repeatButtonViewData: LiveData<RepeatButtonViewData>
        by lazySuspend { loadRepeatButtonViewData() }
    val keepStatisticsRangeCheckbox: LiveData<Boolean>
        by lazySuspend { prefsInteractor.getKeepStatisticsRange() }
    val startOfDayViewData: LiveData<SettingsStartOfDayViewData>
        by lazySuspend { loadStartOfDayViewData() }
    val ignoreShortRecordsViewData: LiveData<String>
        by lazySuspend { loadIgnoreShortRecordsViewData() }
    val showRecordTagSelectionCheckbox: LiveData<Boolean>
        by lazySuspend { prefsInteractor.getShowRecordTagSelection() }
    val recordTagSelectionCloseCheckbox: LiveData<Boolean>
        by lazySuspend { prefsInteractor.getRecordTagSelectionCloseAfterOne() }
    val recordTagSelectionForGeneralTagsCheckbox: LiveData<Boolean>
        by lazySuspend { prefsInteractor.getRecordTagSelectionEvenForGeneralTags() }
    val automatedTrackingSendEventsCheckbox: LiveData<Boolean>
        by lazySuspend { prefsInteractor.getAutomatedTrackingSendEvents() }

    private var parent: SettingsParent? = null

    fun init(parent: SettingsParent) {
        this.parent = parent
    }

    fun onVisible() {
        delegateScope.launch {
            // Update can come from quick settings widget
            showRecordTagSelectionCheckbox.set(prefsInteractor.getShowRecordTagSelection())

            // Update after day changes
            updateStartOfDayViewData()
        }
    }

    suspend fun onUseMilitaryTimeClicked() {
        updateStartOfDayViewData()
    }

    fun onFirstDayOfWeekSelected(position: Int) {
        val newDayOfWeek = settingsMapper.toDayOfWeek(position)

        delegateScope.launch {
            prefsInteractor.setFirstDayOfWeek(newDayOfWeek)
            widgetInteractor.updateWidgets(listOf(WidgetType.STATISTICS_CHART))
            notificationGoalTimeInteractor.checkAndReschedule()
            updateFirstDayOfWeekViewData()
        }
    }

    fun onRepeatButtonSelected(position: Int) {
        val newType = settingsMapper.toRepeatButtonType(position)

        delegateScope.launch {
            prefsInteractor.setRepeatButtonType(newType)
            updateRepeatButtonViewData()
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
            updateStartOfDayViewData()
        }
    }

    fun onKeepStatisticsRangeClicked() {
        delegateScope.launch {
            val newValue = !prefsInteractor.getKeepStatisticsRange()
            prefsInteractor.setKeepStatisticsRange(newValue)
            keepStatisticsRangeCheckbox.set(newValue)
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
            showRecordTagSelectionCheckbox.set(newValue)
        }
    }

    fun onRecordTagSelectionCloseClicked() {
        delegateScope.launch {
            val newValue = !prefsInteractor.getRecordTagSelectionCloseAfterOne()
            prefsInteractor.setRecordTagSelectionCloseAfterOne(newValue)
            recordTagSelectionCloseCheckbox.set(newValue)
        }
    }

    fun onRecordTagSelectionGeneralClicked() {
        delegateScope.launch {
            val newValue = !prefsInteractor.getRecordTagSelectionEvenForGeneralTags()
            prefsInteractor.setRecordTagSelectionEvenForGeneralTags(newValue)
            recordTagSelectionForGeneralTagsCheckbox.set(newValue)
        }
    }

    fun onAutomatedTrackingSendEventsClicked() {
        delegateScope.launch {
            val newValue = !prefsInteractor.getAutomatedTrackingSendEvents()
            prefsInteractor.setAutomatedTrackingSendEvents(newValue)
            automatedTrackingSendEventsCheckbox.set(newValue)
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
                updateIgnoreShortRecordsViewData()
            }
        }
    }

    fun onDurationDisabled(tag: String?) {
        when (tag) {
            SettingsViewModel.IGNORE_SHORT_RECORDS_DIALOG_TAG -> delegateScope.launch {
                prefsInteractor.setIgnoreShortRecordsDuration(0)
                updateIgnoreShortRecordsViewData()
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
                updateStartOfDayViewData()
            }
        }
    }

    private suspend fun updateFirstDayOfWeekViewData() {
        firstDayOfWeekViewData.set(loadFirstDayOfWeekViewData())
    }

    private suspend fun loadFirstDayOfWeekViewData(): FirstDayOfWeekViewData {
        return prefsInteractor.getFirstDayOfWeek()
            .let(settingsMapper::toFirstDayOfWeekViewData)
    }

    private suspend fun updateRepeatButtonViewData() {
        repeatButtonViewData.set(loadRepeatButtonViewData())
    }

    private suspend fun loadRepeatButtonViewData(): RepeatButtonViewData {
        return prefsInteractor.getRepeatButtonType()
            .let(settingsMapper::toRepeatButtonViewData)
    }

    private suspend fun updateStartOfDayViewData() {
        val data = loadStartOfDayViewData()
        startOfDayViewData.set(data)
    }

    private suspend fun loadStartOfDayViewData(): SettingsStartOfDayViewData {
        val shift = prefsInteractor.getStartOfDayShift()
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()

        val hint = resourceRepo.getString(
            R.string.settings_start_of_day_hint_value,
            timeMapper.formatDateTime(
                time = timeMapper.getStartOfDayTimeStamp() + shift,
                useMilitaryTime = useMilitaryTime,
                showSeconds = false,
            ),
        )

        return SettingsStartOfDayViewData(
            startOfDayValue = settingsMapper.toStartOfDayText(shift, useMilitaryTime = true),
            startOfDaySign = settingsMapper.toStartOfDaySign(shift),
            hint = hint,
        )
    }

    private suspend fun updateIgnoreShortRecordsViewData() {
        val data = loadIgnoreShortRecordsViewData()
        ignoreShortRecordsViewData.set(data)
    }

    private suspend fun loadIgnoreShortRecordsViewData(): String {
        return prefsInteractor.getIgnoreShortRecordsDuration()
            .let(settingsMapper::toDurationViewData)
            .text
    }
}