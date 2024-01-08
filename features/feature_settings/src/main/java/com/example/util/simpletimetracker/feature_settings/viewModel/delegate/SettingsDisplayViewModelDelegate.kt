package com.example.util.simpletimetracker.feature_settings.viewModel.delegate

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.core.extension.lazySuspend
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.domain.interactor.NotificationTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.WidgetInteractor
import com.example.util.simpletimetracker.domain.model.CardOrder
import com.example.util.simpletimetracker.domain.model.WidgetTransparencyPercent
import com.example.util.simpletimetracker.domain.model.WidgetType
import com.example.util.simpletimetracker.feature_settings.mapper.SettingsMapper
import com.example.util.simpletimetracker.feature_settings.viewData.CardOrderViewData
import com.example.util.simpletimetracker.feature_settings.viewData.DaysInCalendarViewData
import com.example.util.simpletimetracker.feature_settings.viewData.SettingsUntrackedRangeViewData
import com.example.util.simpletimetracker.feature_settings.viewData.WidgetTransparencyViewData
import com.example.util.simpletimetracker.feature_settings.viewModel.SettingsViewModel
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.CardOrderDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.CardSizeDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.DurationDialogParams
import kotlinx.coroutines.launch
import javax.inject.Inject

class SettingsDisplayViewModelDelegate @Inject constructor(
    private val router: Router,
    private val prefsInteractor: PrefsInteractor,
    private val settingsMapper: SettingsMapper,
    private val notificationTypeInteractor: NotificationTypeInteractor,
    private val widgetInteractor: WidgetInteractor,
) : ViewModelDelegate() {

    val cardOrderViewData: LiveData<CardOrderViewData>
        by lazySuspend { loadCardOrderViewData() }
    val btnCardOrderManualVisibility: LiveData<Boolean>
        by lazySuspend { prefsInteractor.getCardOrder() == CardOrder.MANUAL }
    val showUntrackedInRecordsCheckbox: LiveData<Boolean>
        by lazySuspend { prefsInteractor.getShowUntrackedInRecords() }
    val showUntrackedInStatisticsCheckbox: LiveData<Boolean>
        by lazySuspend { prefsInteractor.getShowUntrackedInStatistics() }
    val ignoreShortUntrackedViewData: LiveData<String>
        by lazySuspend { loadIgnoreShortUntrackedViewData() }
    val untrackedRangeViewData: LiveData<SettingsUntrackedRangeViewData>
        by lazySuspend { loadUntrackedRangeViewData() }
    val showRecordsCalendarCheckbox: LiveData<Boolean>
        by lazySuspend { prefsInteractor.getShowRecordsCalendar() }
    val reverseOrderInCalendarCheckbox: LiveData<Boolean>
        by lazySuspend { prefsInteractor.getReverseOrderInCalendar() }
    val daysInCalendarViewData: LiveData<DaysInCalendarViewData>
        by lazySuspend { loadDaysInCalendarViewData() }
    val widgetTransparencyViewData: LiveData<WidgetTransparencyViewData>
        by lazySuspend { loadWidgetTransparencyViewData() }
    val showActivityFiltersCheckbox: LiveData<Boolean>
        by lazySuspend { prefsInteractor.getShowActivityFilters() }
    val showGoalsSeparatelyCheckbox: LiveData<Boolean>
        by lazySuspend { prefsInteractor.getShowGoalsSeparately() }
    val useMilitaryTimeCheckbox: LiveData<Boolean>
        by lazySuspend { prefsInteractor.getUseMilitaryTimeFormat() }
    val useMilitaryTimeHint: LiveData<String>
        by lazySuspend { loadUseMilitaryTimeViewData() }
    val useMonthDayTimeCheckbox: LiveData<Boolean>
        by lazySuspend { prefsInteractor.getUseMonthDayTimeFormat() }
    val useMonthDayTimeHint: LiveData<String>
        by lazySuspend { loadUseMonthDayTimeViewData() }
    val useProportionalMinutesCheckbox: LiveData<Boolean>
        by lazySuspend { prefsInteractor.getUseProportionalMinutes() }
    val useProportionalMinutesHint: LiveData<String>
        by lazySuspend { loadUseProportionalMinutesViewData() }
    val showSecondsCheckbox: LiveData<Boolean>
        by lazySuspend { prefsInteractor.getShowSeconds() }
    val keepScreenOnCheckbox: LiveData<Boolean>
        by lazySuspend { prefsInteractor.getKeepScreenOn() }

    private var parent: SettingsParent? = null

    fun init(parent: SettingsParent) {
        this.parent = parent
    }

    fun onVisible() {
        delegateScope.launch {
            // Need to update card order because it changes on card order dialog
            updateCardOrderViewData()
        }
    }

    fun onDaysInCalendarSelected(position: Int) {
        delegateScope.launch {
            val currentValue = prefsInteractor.getDaysInCalendar()
            val newValue = settingsMapper.toDaysInCalendar(position)
            if (newValue == currentValue) return@launch
            prefsInteractor.setDaysInCalendar(newValue)
            updateDaysInCalendarViewData()
        }
    }

    fun onWidgetTransparencySelected(position: Int) {
        delegateScope.launch {
            val currentValue = prefsInteractor.getWidgetBackgroundTransparencyPercent()
                .let(::WidgetTransparencyPercent)
            val newValue = settingsMapper.toWidgetTransparency(position)
            if (newValue == currentValue) return@launch
            prefsInteractor.setWidgetBackgroundTransparencyPercent(newValue.value)
            updateWidgetTransparencyViewData()
            widgetInteractor.updateWidgets()
        }
    }

    fun onRecordTypeOrderSelected(position: Int) {
        delegateScope.launch {
            val currentOrder = prefsInteractor.getCardOrder()
            val newOrder = settingsMapper.toCardOrder(position)
            if (newOrder == currentOrder) return@launch

            btnCardOrderManualVisibility.set(newOrder == CardOrder.MANUAL)
            if (newOrder == CardOrder.MANUAL) {
                openCardOrderDialog(currentOrder)
            }
            prefsInteractor.setCardOrder(newOrder)
            updateCardOrderViewData()
        }
    }

    fun onCardOrderManualClick() {
        openCardOrderDialog(CardOrder.MANUAL)
    }

    fun onShowUntrackedInRecordsClicked() {
        delegateScope.launch {
            val newValue = !prefsInteractor.getShowUntrackedInRecords()
            prefsInteractor.setShowUntrackedInRecords(newValue)
            showUntrackedInRecordsCheckbox.set(newValue)
        }
    }

    fun onShowUntrackedInStatisticsClicked() {
        delegateScope.launch {
            val newValue = !prefsInteractor.getShowUntrackedInStatistics()
            prefsInteractor.setShowUntrackedInStatistics(newValue)
            showUntrackedInStatisticsCheckbox.set(newValue)
            widgetInteractor.updateWidgets(listOf(WidgetType.STATISTICS_CHART))
        }
    }

    fun onIgnoreShortUntrackedClicked() {
        delegateScope.launch {
            DurationDialogParams(
                tag = SettingsViewModel.IGNORE_SHORT_UNTRACKED_DIALOG_TAG,
                duration = prefsInteractor.getIgnoreShortUntrackedDuration(),
            ).let(router::navigate)
        }
    }

    fun onUntrackedRangeClicked() {
        delegateScope.launch {
            val newValue = !prefsInteractor.getUntrackedRangeEnabled()
            prefsInteractor.setUntrackedRangeEnabled(newValue)
            updateUntrackedRangeViewData()
        }
    }

    fun onUntrackedRangeStartClicked() {
        delegateScope.launch {
            parent?.openDateTimeDialog(
                tag = SettingsViewModel.UNTRACKED_RANGE_START_DIALOG_TAG,
                timestamp = prefsInteractor.getUntrackedRangeStart(),
                useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat(),
            )
        }
    }

    fun onUntrackedRangeEndClicked() {
        delegateScope.launch {
            parent?.openDateTimeDialog(
                tag = SettingsViewModel.UNTRACKED_RANGE_END_DIALOG_TAG,
                timestamp = prefsInteractor.getUntrackedRangeEnd(),
                useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat(),
            )
        }
    }

    fun onShowRecordsCalendarClicked() {
        delegateScope.launch {
            val newValue = !prefsInteractor.getShowRecordsCalendar()
            prefsInteractor.setShowRecordsCalendar(newValue)
            (showRecordsCalendarCheckbox as MutableLiveData).value = newValue
        }
    }

    fun onReverseOrderInCalendarClicked() {
        delegateScope.launch {
            val newValue = !prefsInteractor.getReverseOrderInCalendar()
            prefsInteractor.setReverseOrderInCalendar(newValue)
            (reverseOrderInCalendarCheckbox as MutableLiveData).value = newValue
        }
    }

    fun onShowActivityFiltersClicked() {
        delegateScope.launch {
            val newValue = !prefsInteractor.getShowActivityFilters()
            prefsInteractor.setShowActivityFilters(newValue)
            (showActivityFiltersCheckbox as MutableLiveData).value = newValue
        }
    }

    fun onShowGoalsSeparatelyClicked() {
        delegateScope.launch {
            val newValue = !prefsInteractor.getShowGoalsSeparately()
            prefsInteractor.setShowGoalsSeparately(newValue)
            (showGoalsSeparatelyCheckbox as MutableLiveData).value = newValue
            router.restartApp()
        }
    }

    fun onUseMilitaryTimeClicked() {
        delegateScope.launch {
            val newValue = !prefsInteractor.getUseMilitaryTimeFormat()
            prefsInteractor.setUseMilitaryTimeFormat(newValue)
            (useMilitaryTimeCheckbox as MutableLiveData).value = newValue
            notificationTypeInteractor.updateNotifications()
            updateUseMilitaryTimeViewData()
            updateUntrackedRangeViewData()
            parent?.onUseMilitaryTimeClicked()
            parent?.updateContent()
        }
    }

    fun onUseMonthDayTimeClicked() {
        delegateScope.launch {
            val newValue = !prefsInteractor.getUseMonthDayTimeFormat()
            prefsInteractor.setUseMonthDayTimeFormat(newValue)
            (useMonthDayTimeCheckbox as MutableLiveData).value = newValue
            updateUseMonthDayTimeViewData()
        }
    }

    fun onUseProportionalMinutesClicked() {
        delegateScope.launch {
            val newValue = !prefsInteractor.getUseProportionalMinutes()
            prefsInteractor.setUseProportionalMinutes(newValue)
            (useProportionalMinutesCheckbox as MutableLiveData).value = newValue
            notificationTypeInteractor.updateNotifications()
            widgetInteractor.updateWidgets(listOf(WidgetType.STATISTICS_CHART))
            updateUseProportionalMinutesViewData()
        }
    }

    fun onShowSecondsClicked() {
        delegateScope.launch {
            val newValue = !prefsInteractor.getShowSeconds()
            prefsInteractor.setShowSeconds(newValue)
            showSecondsCheckbox.set(newValue)
            notificationTypeInteractor.updateNotifications()
            widgetInteractor.updateWidgets(listOf(WidgetType.STATISTICS_CHART))
        }
    }

    fun onKeepScreenOnClicked() {
        delegateScope.launch {
            val newValue = !prefsInteractor.getKeepScreenOn()
            prefsInteractor.setKeepScreenOn(newValue)
            (keepScreenOnCheckbox as MutableLiveData).value = newValue
        }
    }

    fun onChangeCardSizeClick() {
        router.navigate(CardSizeDialogParams)
    }

    fun onDurationSet(tag: String?, duration: Long) {
        when (tag) {
            SettingsViewModel.IGNORE_SHORT_UNTRACKED_DIALOG_TAG -> delegateScope.launch {
                prefsInteractor.setIgnoreShortUntrackedDuration(duration)
                updateIgnoreShortUntrackedViewData()
            }
        }
    }

    fun onDurationDisabled(tag: String?) {
        when (tag) {
            SettingsViewModel.IGNORE_SHORT_UNTRACKED_DIALOG_TAG -> delegateScope.launch {
                prefsInteractor.setIgnoreShortUntrackedDuration(0)
                updateIgnoreShortUntrackedViewData()
            }
        }
    }

    fun onDateTimeSet(timestamp: Long, tag: String?) = delegateScope.launch {
        when (tag) {
            SettingsViewModel.UNTRACKED_RANGE_START_DIALOG_TAG -> {
                val newValue = settingsMapper.toStartOfDayShift(timestamp, wasPositive = true)
                prefsInteractor.setUntrackedRangeStart(newValue)
                updateUntrackedRangeViewData()
            }

            SettingsViewModel.UNTRACKED_RANGE_END_DIALOG_TAG -> {
                val newValue = settingsMapper.toStartOfDayShift(timestamp, wasPositive = true)
                prefsInteractor.setUntrackedRangeEnd(newValue)
                updateUntrackedRangeViewData()
            }
        }
    }

    private fun openCardOrderDialog(cardOrder: CardOrder) {
        router.navigate(
            CardOrderDialogParams(cardOrder),
        )
    }

    private suspend fun updateDaysInCalendarViewData() {
        val data = loadDaysInCalendarViewData()
        daysInCalendarViewData.set(data)
    }

    private suspend fun loadDaysInCalendarViewData(): DaysInCalendarViewData {
        return prefsInteractor.getDaysInCalendar()
            .let(settingsMapper::toDaysInCalendarViewData)
    }

    private suspend fun updateWidgetTransparencyViewData() {
        val data = loadWidgetTransparencyViewData()
        widgetTransparencyViewData.set(data)
    }

    private suspend fun loadWidgetTransparencyViewData(): WidgetTransparencyViewData {
        return prefsInteractor.getWidgetBackgroundTransparencyPercent()
            .let(::WidgetTransparencyPercent)
            .let(settingsMapper::toWidgetTransparencyViewData)
    }

    private suspend fun updateCardOrderViewData() {
        val data = loadCardOrderViewData()
        (cardOrderViewData as MutableLiveData).value = data
    }

    private suspend fun loadCardOrderViewData(): CardOrderViewData {
        return prefsInteractor.getCardOrder()
            .let(settingsMapper::toCardOrderViewData)
    }

    private suspend fun updateIgnoreShortUntrackedViewData() {
        val data = loadIgnoreShortUntrackedViewData()
        ignoreShortUntrackedViewData.set(data)
    }

    private suspend fun loadIgnoreShortUntrackedViewData(): String {
        return prefsInteractor.getIgnoreShortUntrackedDuration()
            .let(settingsMapper::toDurationViewData)
            .text
    }

    private suspend fun updateUntrackedRangeViewData() {
        val data = loadUntrackedRangeViewData()
        untrackedRangeViewData.set(data)
    }

    private suspend fun loadUntrackedRangeViewData(): SettingsUntrackedRangeViewData {
        val enabled = prefsInteractor.getUntrackedRangeEnabled()

        return if (enabled) {
            val start = prefsInteractor.getUntrackedRangeStart()
            val end = prefsInteractor.getUntrackedRangeEnd()
            val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
            SettingsUntrackedRangeViewData.Enabled(
                settingsMapper.toStartOfDayText(start, useMilitaryTime),
                settingsMapper.toStartOfDayText(end, useMilitaryTime),
            )
        } else {
            SettingsUntrackedRangeViewData.Disabled
        }
    }

    private suspend fun updateUseMilitaryTimeViewData() {
        val data = loadUseMilitaryTimeViewData()
        useMilitaryTimeHint.set(data)
    }

    private suspend fun loadUseMilitaryTimeViewData(): String {
        return prefsInteractor.getUseMilitaryTimeFormat()
            .let(settingsMapper::toUseMilitaryTimeHint)
    }

    private suspend fun updateUseMonthDayTimeViewData() {
        val data = loadUseMonthDayTimeViewData()
        useMonthDayTimeHint.set(data)
    }

    private suspend fun loadUseMonthDayTimeViewData(): String {
        return prefsInteractor.getUseMonthDayTimeFormat()
            .let(settingsMapper::toUseMonthDayTimeHint)
    }

    private suspend fun updateUseProportionalMinutesViewData() {
        val data = loadUseProportionalMinutesViewData()
        useProportionalMinutesHint.set(data)
    }

    private suspend fun loadUseProportionalMinutesViewData(): String {
        return prefsInteractor.getUseProportionalMinutes()
            .let(settingsMapper::toUseProportionalMinutesHint)
    }
}