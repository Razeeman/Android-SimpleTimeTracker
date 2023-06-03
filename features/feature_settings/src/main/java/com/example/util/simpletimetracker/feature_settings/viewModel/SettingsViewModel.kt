package com.example.util.simpletimetracker.feature_settings.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.BuildConfig
import com.example.util.simpletimetracker.core.base.SingleLiveEvent
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.interactor.CheckExactAlarmPermissionInteractor
import com.example.util.simpletimetracker.core.model.NavigationTab
import com.example.util.simpletimetracker.core.provider.ApplicationDataProvider
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.extension.flip
import com.example.util.simpletimetracker.domain.extension.orFalse
import com.example.util.simpletimetracker.domain.interactor.NotificationActivityInteractor
import com.example.util.simpletimetracker.domain.interactor.NotificationGoalTimeInteractor
import com.example.util.simpletimetracker.domain.interactor.NotificationInactivityInteractor
import com.example.util.simpletimetracker.domain.interactor.NotificationTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.WidgetInteractor
import com.example.util.simpletimetracker.domain.model.CardOrder
import com.example.util.simpletimetracker.domain.model.WidgetType
import com.example.util.simpletimetracker.feature_settings.R
import com.example.util.simpletimetracker.feature_settings.mapper.SettingsMapper
import com.example.util.simpletimetracker.feature_settings.viewData.CardOrderViewData
import com.example.util.simpletimetracker.feature_settings.viewData.DaysInCalendarViewData
import com.example.util.simpletimetracker.feature_settings.viewData.FirstDayOfWeekViewData
import com.example.util.simpletimetracker.feature_settings.viewData.SettingsDurationViewData
import com.example.util.simpletimetracker.feature_settings.viewData.SettingsStartOfDayViewData
import com.example.util.simpletimetracker.feature_settings.viewData.SettingsUntrackedRangeViewData
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.action.OpenMarketParams
import com.example.util.simpletimetracker.navigation.params.action.SendEmailParams
import com.example.util.simpletimetracker.navigation.params.notification.SnackBarParams
import com.example.util.simpletimetracker.navigation.params.screen.ArchiveParams
import com.example.util.simpletimetracker.navigation.params.screen.CardOrderDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.CardSizeDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.CategoriesParams
import com.example.util.simpletimetracker.navigation.params.screen.DataEditParams
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogType
import com.example.util.simpletimetracker.navigation.params.screen.DurationDialogParams
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val router: Router,
    private val resourceRepo: ResourceRepo,
    private val prefsInteractor: PrefsInteractor,
    private val settingsMapper: SettingsMapper,
    private val applicationDataProvider: ApplicationDataProvider,
    private val notificationTypeInteractor: NotificationTypeInteractor,
    private val widgetInteractor: WidgetInteractor,
    private val notificationInactivityInteractor: NotificationInactivityInteractor,
    private val notificationActivityInteractor: NotificationActivityInteractor,
    private val notificationGoalTimeInteractor: NotificationGoalTimeInteractor,
    private val checkExactAlarmPermissionInteractor: CheckExactAlarmPermissionInteractor,
) : ViewModel() {

    val daysInCalendarViewData: LiveData<DaysInCalendarViewData> by lazy {
        MutableLiveData<DaysInCalendarViewData>().let { initial ->
            viewModelScope.launch {
                initial.value = loadDaysInCalendarViewData()
            }
            initial
        }
    }

    val cardOrderViewData: LiveData<CardOrderViewData> by lazy {
        MutableLiveData<CardOrderViewData>().let { initial ->
            viewModelScope.launch {
                initial.value = loadCardOrderViewData()
            }
            initial
        }
    }

    val btnCardOrderManualVisibility: LiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().let { initial ->
            viewModelScope.launch {
                initial.value = prefsInteractor.getCardOrder() == CardOrder.MANUAL
            }
            initial
        }
    }

    val firstDayOfWeekViewData: LiveData<FirstDayOfWeekViewData> by lazy {
        MutableLiveData<FirstDayOfWeekViewData>().let { initial ->
            viewModelScope.launch {
                initial.value = loadFirstDayOfWeekViewData()
            }
            initial
        }
    }

    val showUntrackedCheckbox: LiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().let { initial ->
            viewModelScope.launch {
                initial.value = prefsInteractor.getShowUntrackedInRecords()
            }
            initial
        }
    }

    val untrackedRangeViewData: LiveData<SettingsUntrackedRangeViewData> by lazy {
        MutableLiveData<SettingsUntrackedRangeViewData>().let { initial ->
            viewModelScope.launch {
                initial.value = loadUntrackedRangeViewData()
            }
            initial
        }
    }

    val showRecordsCalendarCheckbox: LiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().let { initial ->
            viewModelScope.launch {
                initial.value = prefsInteractor.getShowRecordsCalendar()
            }
            initial
        }
    }

    val reverseOrderInCalendarCheckbox: LiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().let { initial ->
            viewModelScope.launch {
                initial.value = prefsInteractor.getReverseOrderInCalendar()
            }
            initial
        }
    }

    val showActivityFiltersCheckbox: LiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().let { initial ->
            viewModelScope.launch {
                initial.value = prefsInteractor.getShowActivityFilters()
            }
            initial
        }
    }

    val allowMultitaskingCheckbox: LiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().let { initial ->
            viewModelScope.launch {
                initial.value = prefsInteractor.getAllowMultitasking()
            }
            initial
        }
    }

    val showNotificationsCheckbox: LiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().let { initial ->
            viewModelScope.launch {
                initial.value = prefsInteractor.getShowNotifications()
            }
            initial
        }
    }

    val showNotificationsControlsCheckbox: LiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().let { initial ->
            viewModelScope.launch {
                initial.value = prefsInteractor.getShowNotificationsControls()
            }
            initial
        }
    }

    val keepStatisticsRangeCheckbox: LiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().let { initial ->
            viewModelScope.launch {
                initial.value = prefsInteractor.getKeepStatisticsRange()
            }
            initial
        }
    }

    val startOfDayViewData: LiveData<SettingsStartOfDayViewData> by lazy {
        MutableLiveData<SettingsStartOfDayViewData>().let { initial ->
            viewModelScope.launch {
                initial.value = loadStartOfDayViewData()
            }
            initial
        }
    }

    val inactivityReminderViewData: LiveData<SettingsDurationViewData> by lazy {
        MutableLiveData<SettingsDurationViewData>().let { initial ->
            viewModelScope.launch {
                initial.value = loadInactivityReminderViewData()
            }
            initial
        }
    }

    val inactivityReminderRecurrentCheckbox: LiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().let { initial ->
            viewModelScope.launch {
                initial.value = prefsInteractor.getInactivityReminderRecurrent()
            }
            initial
        }
    }

    val inactivityReminderDndStartViewData: LiveData<String> by lazy {
        MutableLiveData<String>().let { initial ->
            viewModelScope.launch {
                initial.value = loadInactivityReminderDndStartViewData()
            }
            initial
        }
    }

    val inactivityReminderDndEndViewData: LiveData<String> by lazy {
        MutableLiveData<String>().let { initial ->
            viewModelScope.launch {
                initial.value = loadInactivityReminderDndEndViewData()
            }
            initial
        }
    }

    val activityReminderViewData: LiveData<SettingsDurationViewData> by lazy {
        MutableLiveData<SettingsDurationViewData>().let { initial ->
            viewModelScope.launch {
                initial.value = loadActivityReminderViewData()
            }
            initial
        }
    }

    val activityReminderRecurrentCheckbox: LiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().let { initial ->
            viewModelScope.launch {
                initial.value = prefsInteractor.getActivityReminderRecurrent()
            }
            initial
        }
    }

    val activityReminderDndStartViewData: LiveData<String> by lazy {
        MutableLiveData<String>().let { initial ->
            viewModelScope.launch {
                initial.value = loadActivityReminderDndStartViewData()
            }
            initial
        }
    }

    val activityReminderDndEndViewData: LiveData<String> by lazy {
        MutableLiveData<String>().let { initial ->
            viewModelScope.launch {
                initial.value = loadActivityReminderDndEndViewData()
            }
            initial
        }
    }

    val ignoreShortRecordsViewData: LiveData<String> by lazy {
        MutableLiveData<String>().let { initial ->
            viewModelScope.launch {
                initial.value = loadIgnoreShortRecordsViewData()
            }
            initial
        }
    }

    val ignoreShortUntrackedViewData: LiveData<String> by lazy {
        MutableLiveData<String>().let { initial ->
            viewModelScope.launch {
                initial.value = loadIgnoreShortUntrackedViewData()
            }
            initial
        }
    }

    val darkModeCheckbox: LiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().let { initial ->
            viewModelScope.launch {
                initial.value = prefsInteractor.getDarkMode()
            }
            initial
        }
    }

    val showRecordTagSelectionCheckbox: LiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().let { initial ->
            viewModelScope.launch {
                initial.value = prefsInteractor.getShowRecordTagSelection()
            }
            initial
        }
    }

    val recordTagSelectionCloseCheckbox: LiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().let { initial ->
            viewModelScope.launch {
                initial.value = prefsInteractor.getRecordTagSelectionCloseAfterOne()
            }
            initial
        }
    }

    val recordTagSelectionForGeneralTagsCheckbox: LiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().let { initial ->
            viewModelScope.launch {
                initial.value = prefsInteractor.getRecordTagSelectionEvenForGeneralTags()
            }
            initial
        }
    }

    val automatedTrackingSendEventsCheckbox: LiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().let { initial ->
            viewModelScope.launch {
                initial.value = prefsInteractor.getAutomatedTrackingSendEvents()
            }
            initial
        }
    }

    val useMilitaryTimeCheckbox: LiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().let { initial ->
            viewModelScope.launch {
                initial.value = prefsInteractor.getUseMilitaryTimeFormat()
            }
            initial
        }
    }

    val useMilitaryTimeHint: LiveData<String> by lazy {
        MutableLiveData<String>().let { initial ->
            viewModelScope.launch {
                initial.value = loadUseMilitaryTimeViewData()
            }
            initial
        }
    }

    val useProportionalMinutesCheckbox: LiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().let { initial ->
            viewModelScope.launch {
                initial.value = prefsInteractor.getUseProportionalMinutes()
            }
            initial
        }
    }

    val showSecondsCheckbox: LiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().let { initial ->
            viewModelScope.launch {
                initial.value = prefsInteractor.getShowSeconds()
            }
            initial
        }
    }

    val keepScreenOnCheckbox: LiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().let { initial ->
            viewModelScope.launch {
                initial.value = prefsInteractor.getKeepScreenOn()
            }
            initial
        }
    }

    val useProportionalMinutesHint: LiveData<String> by lazy {
        MutableLiveData<String>().let { initial ->
            viewModelScope.launch {
                initial.value = loadUseProportionalMinutesViewData()
            }
            initial
        }
    }

    val versionName: LiveData<String> by lazy {
        MutableLiveData(loadVersionName())
    }

    val themeChanged: LiveData<Boolean> = MutableLiveData(false)
    val settingsNotificationsVisibility: LiveData<Boolean> = MutableLiveData(false)
    val settingsDisplayVisibility: LiveData<Boolean> = MutableLiveData(false)
    val settingsAdditionalVisibility: LiveData<Boolean> = MutableLiveData(false)
    val settingsBackupVisibility: LiveData<Boolean> = MutableLiveData(false)
    val resetScreen: SingleLiveEvent<Unit> = SingleLiveEvent()

    fun onVisible() {
        viewModelScope.launch {
            // Need to update card order because it changes on card order dialog
            updateCardOrderViewData()

            // Update can come from quick settings widget
            allowMultitaskingCheckbox.set(prefsInteractor.getAllowMultitasking())
            showRecordTagSelectionCheckbox.set(prefsInteractor.getShowRecordTagSelection())
        }
    }

    fun onSettingsNotificationsClick() {
        val newValue = settingsNotificationsVisibility.value?.flip().orFalse()
        settingsNotificationsVisibility.set(newValue)
    }

    fun onSettingsDisplayClick() {
        val newValue = settingsDisplayVisibility.value?.flip().orFalse()
        settingsDisplayVisibility.set(newValue)
    }

    fun onSettingsAdditionalClick() {
        val newValue = settingsAdditionalVisibility.value?.flip().orFalse()
        settingsAdditionalVisibility.set(newValue)
    }

    fun onSettingsBackupClick() {
        val newValue = settingsBackupVisibility.value?.flip().orFalse()
        settingsBackupVisibility.set(newValue)
    }

    fun onRateClick() {
        router.execute(OpenMarketParams(packageName = applicationDataProvider.getPackageName()))
    }

    fun onFeedbackClick() {
        router.execute(
            data = SendEmailParams(
                email = resourceRepo.getString(R.string.support_email),
                subject = resourceRepo.getString(R.string.support_email_subject),
                chooserTitle = resourceRepo.getString(R.string.settings_email_chooser_title),
                notHandledCallback = { R.string.message_app_not_found.let(::showMessage) }
            )
        )
    }

    fun onDaysInCalendarSelected(position: Int) {
        viewModelScope.launch {
            val currentValue = prefsInteractor.getDaysInCalendar()
            val newValue = settingsMapper.toDaysInCalendar(position)
            if (newValue == currentValue) return@launch
            prefsInteractor.setDaysInCalendar(newValue)
            updateDaysInCalendarViewData()
        }
    }

    fun onRecordTypeOrderSelected(position: Int) {
        viewModelScope.launch {
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

    fun onFirstDayOfWeekSelected(position: Int) {
        val newDayOfWeek = settingsMapper.toDayOfWeek(position)

        viewModelScope.launch {
            prefsInteractor.setFirstDayOfWeek(newDayOfWeek)
            widgetInteractor.updateWidgets(listOf(WidgetType.STATISTICS_CHART))
            notificationGoalTimeInteractor.checkAndReschedule()
            updateFirstDayOfWeekViewData()
        }
    }

    fun onStartOfDayClicked() {
        viewModelScope.launch {
            openDateTimeDialog(
                tag = START_OF_DAY_DIALOG_TAG,
                timestamp = prefsInteractor.getStartOfDayShift(),
                useMilitaryTime = true,
            )
        }
    }

    fun onStartOfDaySignClicked() {
        viewModelScope.launch {
            val newValue = prefsInteractor.getStartOfDayShift() * -1
            prefsInteractor.setStartOfDayShift(newValue)
            widgetInteractor.updateWidgets(listOf(WidgetType.STATISTICS_CHART))
            notificationGoalTimeInteractor.checkAndReschedule()
            updateStartOfDayViewData()
        }
    }

    fun onShowUntrackedClicked() {
        viewModelScope.launch {
            val newValue = !prefsInteractor.getShowUntrackedInRecords()
            prefsInteractor.setShowUntrackedInRecords(newValue)
            (showUntrackedCheckbox as MutableLiveData).value = newValue
        }
    }

    fun onUntrackedRangeClicked() {
        viewModelScope.launch {
            val newValue = !prefsInteractor.getUntrackedRangeEnabled()
            prefsInteractor.setUntrackedRangeEnabled(newValue)
            updateUntrackedRangeViewData()
        }
    }

    fun onUntrackedRangeStartClicked() {
        viewModelScope.launch {
            openDateTimeDialog(
                tag = UNTRACKED_RANGE_START_DIALOG_TAG,
                timestamp = prefsInteractor.getUntrackedRangeStart(),
                useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat(),
            )
        }
    }

    fun onUntrackedRangeEndClicked() {
        viewModelScope.launch {
            openDateTimeDialog(
                tag = UNTRACKED_RANGE_END_DIALOG_TAG,
                timestamp = prefsInteractor.getUntrackedRangeEnd(),
                useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat(),
            )
        }
    }

    fun onShowRecordsCalendarClicked() {
        viewModelScope.launch {
            val newValue = !prefsInteractor.getShowRecordsCalendar()
            prefsInteractor.setShowRecordsCalendar(newValue)
            (showRecordsCalendarCheckbox as MutableLiveData).value = newValue
        }
    }

    fun onReverseOrderInCalendarClicked() {
        viewModelScope.launch {
            val newValue = !prefsInteractor.getReverseOrderInCalendar()
            prefsInteractor.setReverseOrderInCalendar(newValue)
            (reverseOrderInCalendarCheckbox as MutableLiveData).value = newValue
        }
    }

    fun onShowActivityFiltersClicked() {
        viewModelScope.launch {
            val newValue = !prefsInteractor.getShowActivityFilters()
            prefsInteractor.setShowActivityFilters(newValue)
            (showActivityFiltersCheckbox as MutableLiveData).value = newValue
        }
    }

    fun onAllowMultitaskingClicked() {
        viewModelScope.launch {
            val newValue = !prefsInteractor.getAllowMultitasking()
            prefsInteractor.setAllowMultitasking(newValue)
            widgetInteractor.updateWidgets(listOf(WidgetType.QUICK_SETTINGS))
            allowMultitaskingCheckbox.set(newValue)
        }
    }

    fun onShowNotificationsClicked() {
        viewModelScope.launch {
            val newValue = !prefsInteractor.getShowNotifications()
            prefsInteractor.setShowNotifications(newValue)
            showNotificationsCheckbox.set(newValue)
            notificationTypeInteractor.updateNotifications()
        }
    }

    fun onShowNotificationsControlsClicked() {
        viewModelScope.launch {
            val newValue = !prefsInteractor.getShowNotificationsControls()
            prefsInteractor.setShowNotificationsControls(newValue)
            showNotificationsControlsCheckbox.set(newValue)
            notificationTypeInteractor.updateNotifications()
        }
    }

    fun onKeepStatisticsRangeClicked() {
        viewModelScope.launch {
            val newValue = !prefsInteractor.getKeepStatisticsRange()
            prefsInteractor.setKeepStatisticsRange(newValue)
            keepStatisticsRangeCheckbox.set(newValue)
        }
    }

    fun onInactivityReminderClicked() {
        viewModelScope.launch {
            DurationDialogParams(
                tag = INACTIVITY_DURATION_DIALOG_TAG,
                duration = prefsInteractor.getInactivityReminderDuration()
            ).let(router::navigate)
        }
    }

    fun onInactivityReminderRecurrentClicked() {
        viewModelScope.launch {
            val newValue = !prefsInteractor.getInactivityReminderRecurrent()
            prefsInteractor.setInactivityReminderRecurrent(newValue)
            inactivityReminderRecurrentCheckbox.set(newValue)
            notificationInactivityInteractor.cancel()
            notificationInactivityInteractor.checkAndSchedule()
        }
    }

    fun onInactivityReminderDoNotDisturbStartClicked() {
        viewModelScope.launch {
            openDateTimeDialog(
                tag = INACTIVITY_REMINDER_DND_START_DIALOG_TAG,
                timestamp = prefsInteractor.getInactivityReminderDoNotDisturbStart(),
                useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat(),
            )
        }
    }

    fun onInactivityReminderDoNotDisturbEndClicked() {
        viewModelScope.launch {
            openDateTimeDialog(
                tag = INACTIVITY_REMINDER_DND_END_DIALOG_TAG,
                timestamp = prefsInteractor.getInactivityReminderDoNotDisturbEnd(),
                useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat(),
            )
        }
    }

    fun onActivityReminderClicked() {
        viewModelScope.launch {
            DurationDialogParams(
                tag = ACTIVITY_DURATION_DIALOG_TAG,
                duration = prefsInteractor.getActivityReminderDuration()
            ).let(router::navigate)
        }
    }

    fun onActivityReminderRecurrentClicked() {
        viewModelScope.launch {
            val newValue = !prefsInteractor.getActivityReminderRecurrent()
            prefsInteractor.setActivityReminderRecurrent(newValue)
            activityReminderRecurrentCheckbox.set(newValue)
            notificationActivityInteractor.cancel()
            notificationActivityInteractor.checkAndSchedule()
        }
    }

    fun onActivityReminderDoNotDisturbStartClicked() {
        viewModelScope.launch {
            openDateTimeDialog(
                tag = ACTIVITY_REMINDER_DND_START_DIALOG_TAG,
                timestamp = prefsInteractor.getActivityReminderDoNotDisturbStart(),
                useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat(),
            )
        }
    }

    fun onActivityReminderDoNotDisturbEndClicked() {
        viewModelScope.launch {
            openDateTimeDialog(
                tag = ACTIVITY_REMINDER_DND_END_DIALOG_TAG,
                timestamp = prefsInteractor.getActivityReminderDoNotDisturbEnd(),
                useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat(),
            )
        }
    }

    fun onIgnoreShortRecordsClicked() {
        viewModelScope.launch {
            DurationDialogParams(
                tag = IGNORE_SHORT_RECORDS_DIALOG_TAG,
                duration = prefsInteractor.getIgnoreShortRecordsDuration()
            ).let(router::navigate)
        }
    }

    fun onIgnoreShortUntrackedClicked() {
        viewModelScope.launch {
            DurationDialogParams(
                tag = IGNORE_SHORT_UNTRACKED_DIALOG_TAG,
                duration = prefsInteractor.getIgnoreShortUntrackedDuration()
            ).let(router::navigate)
        }
    }

    fun onDarkModeClicked() {
        viewModelScope.launch {
            val newValue = !prefsInteractor.getDarkMode()
            prefsInteractor.setDarkMode(newValue)
            (darkModeCheckbox as MutableLiveData).value = newValue
            (themeChanged as MutableLiveData).value = true
        }
    }

    fun onUseMilitaryTimeClicked() {
        viewModelScope.launch {
            val newValue = !prefsInteractor.getUseMilitaryTimeFormat()
            prefsInteractor.setUseMilitaryTimeFormat(newValue)
            (useMilitaryTimeCheckbox as MutableLiveData).value = newValue
            notificationTypeInteractor.updateNotifications()
            updateUseMilitaryTimeViewData()

            updateStartOfDayViewData()
            updateActivityReminderDndStartViewData()
            updateActivityReminderDndEndViewData()
            updateInactivityReminderDndStartViewData()
            updateInactivityReminderDndEndViewData()
            updateUntrackedRangeViewData()
        }
    }

    fun onUseProportionalMinutesClicked() {
        viewModelScope.launch {
            val newValue = !prefsInteractor.getUseProportionalMinutes()
            prefsInteractor.setUseProportionalMinutes(newValue)
            (useProportionalMinutesCheckbox as MutableLiveData).value = newValue
            notificationTypeInteractor.updateNotifications()
            widgetInteractor.updateWidgets(listOf(WidgetType.STATISTICS_CHART))
            updateUseProportionalMinutesViewData()
        }
    }

    fun onShowSecondsClicked() {
        viewModelScope.launch {
            val newValue = !prefsInteractor.getShowSeconds()
            prefsInteractor.setShowSeconds(newValue)
            showSecondsCheckbox.set(newValue)
            notificationTypeInteractor.updateNotifications()
            widgetInteractor.updateWidgets(listOf(WidgetType.STATISTICS_CHART))
        }
    }

    fun onKeepScreenOnClicked() {
        viewModelScope.launch {
            val newValue = !prefsInteractor.getKeepScreenOn()
            prefsInteractor.setKeepScreenOn(newValue)
            (keepScreenOnCheckbox as MutableLiveData).value = newValue
        }
    }

    fun onShowRecordTagSelectionClicked() {
        viewModelScope.launch {
            val newValue = !prefsInteractor.getShowRecordTagSelection()
            prefsInteractor.setShowRecordTagSelection(newValue)
            widgetInteractor.updateWidgets(listOf(WidgetType.QUICK_SETTINGS))
            showRecordTagSelectionCheckbox.set(newValue)
        }
    }

    fun onRecordTagSelectionCloseClicked() {
        viewModelScope.launch {
            val newValue = !prefsInteractor.getRecordTagSelectionCloseAfterOne()
            prefsInteractor.setRecordTagSelectionCloseAfterOne(newValue)
            recordTagSelectionCloseCheckbox.set(newValue)
        }
    }

    fun onRecordTagSelectionGeneralClicked() {
        viewModelScope.launch {
            val newValue = !prefsInteractor.getRecordTagSelectionEvenForGeneralTags()
            prefsInteractor.setRecordTagSelectionEvenForGeneralTags(newValue)
            recordTagSelectionForGeneralTagsCheckbox.set(newValue)
        }
    }

    fun onAutomatedTrackingSendEventsClicked() {
        viewModelScope.launch {
            val newValue = !prefsInteractor.getAutomatedTrackingSendEvents()
            prefsInteractor.setAutomatedTrackingSendEvents(newValue)
            automatedTrackingSendEventsCheckbox.set(newValue)
        }
    }

    fun onChangeCardSizeClick() {
        router.navigate(CardSizeDialogParams)
    }

    fun onEditCategoriesClick() {
        router.navigate(CategoriesParams)
    }

    fun onArchiveClick() {
        router.navigate(ArchiveParams)
    }

    fun onDataEditClick() {
        router.navigate(DataEditParams)
    }

    fun onDurationSet(tag: String?, duration: Long) {
        when (tag) {
            INACTIVITY_DURATION_DIALOG_TAG -> viewModelScope.launch {
                prefsInteractor.setInactivityReminderDuration(duration)
                updateInactivityReminderViewData()
                notificationInactivityInteractor.cancel()
                notificationInactivityInteractor.checkAndSchedule()
                checkExactAlarmPermissionInteractor.execute()
            }
            ACTIVITY_DURATION_DIALOG_TAG -> viewModelScope.launch {
                prefsInteractor.setActivityReminderDuration(duration)
                updateActivityReminderViewData()
                notificationActivityInteractor.cancel()
                notificationActivityInteractor.checkAndSchedule()
                checkExactAlarmPermissionInteractor.execute()
            }
            IGNORE_SHORT_RECORDS_DIALOG_TAG -> viewModelScope.launch {
                prefsInteractor.setIgnoreShortRecordsDuration(duration)
                updateIgnoreShortRecordsViewData()
            }
            IGNORE_SHORT_UNTRACKED_DIALOG_TAG -> viewModelScope.launch {
                prefsInteractor.setIgnoreShortUntrackedDuration(duration)
                updateIgnoreShortUntrackedViewData()
            }
        }
    }

    fun onDurationDisabled(tag: String?) {
        when (tag) {
            INACTIVITY_DURATION_DIALOG_TAG -> viewModelScope.launch {
                prefsInteractor.setInactivityReminderDuration(0)
                updateInactivityReminderViewData()
                notificationInactivityInteractor.cancel()
            }
            ACTIVITY_DURATION_DIALOG_TAG -> viewModelScope.launch {
                prefsInteractor.setActivityReminderDuration(0)
                updateActivityReminderViewData()
                notificationActivityInteractor.cancel()
            }
            IGNORE_SHORT_RECORDS_DIALOG_TAG -> viewModelScope.launch {
                prefsInteractor.setIgnoreShortRecordsDuration(0)
                updateIgnoreShortRecordsViewData()
            }
            IGNORE_SHORT_UNTRACKED_DIALOG_TAG -> viewModelScope.launch {
                prefsInteractor.setIgnoreShortUntrackedDuration(0)
                updateIgnoreShortUntrackedViewData()
            }
        }
    }

    fun onDateTimeSet(timestamp: Long, tag: String?) = viewModelScope.launch {
        when (tag) {
            START_OF_DAY_DIALOG_TAG -> {
                val wasPositive = prefsInteractor.getStartOfDayShift() >= 0
                val newValue = settingsMapper.toStartOfDayShift(timestamp, wasPositive)
                prefsInteractor.setStartOfDayShift(newValue)
                widgetInteractor.updateWidgets(listOf(WidgetType.STATISTICS_CHART))
                notificationGoalTimeInteractor.checkAndReschedule()
                updateStartOfDayViewData()
            }
            INACTIVITY_REMINDER_DND_START_DIALOG_TAG -> {
                val newValue = settingsMapper.toStartOfDayShift(timestamp, wasPositive = true)
                prefsInteractor.setInactivityReminderDoNotDisturbStart(newValue)
                updateInactivityReminderDndStartViewData()
                notificationInactivityInteractor.cancel()
                notificationInactivityInteractor.checkAndSchedule()
            }
            INACTIVITY_REMINDER_DND_END_DIALOG_TAG -> {
                val newValue = settingsMapper.toStartOfDayShift(timestamp, wasPositive = true)
                prefsInteractor.setInactivityReminderDoNotDisturbEnd(newValue)
                updateInactivityReminderDndEndViewData()
                notificationInactivityInteractor.cancel()
                notificationInactivityInteractor.checkAndSchedule()
            }
            ACTIVITY_REMINDER_DND_START_DIALOG_TAG -> {
                val newValue = settingsMapper.toStartOfDayShift(timestamp, wasPositive = true)
                prefsInteractor.setActivityReminderDoNotDisturbStart(newValue)
                updateActivityReminderDndStartViewData()
                notificationActivityInteractor.cancel()
                notificationActivityInteractor.checkAndSchedule()
            }
            ACTIVITY_REMINDER_DND_END_DIALOG_TAG -> {
                val newValue = settingsMapper.toStartOfDayShift(timestamp, wasPositive = true)
                prefsInteractor.setActivityReminderDoNotDisturbEnd(newValue)
                updateActivityReminderDndEndViewData()
                notificationActivityInteractor.cancel()
                notificationActivityInteractor.checkAndSchedule()
            }
            UNTRACKED_RANGE_START_DIALOG_TAG -> {
                val newValue = settingsMapper.toStartOfDayShift(timestamp, wasPositive = true)
                prefsInteractor.setUntrackedRangeStart(newValue)
                updateUntrackedRangeViewData()
            }
            UNTRACKED_RANGE_END_DIALOG_TAG -> {
                val newValue = settingsMapper.toStartOfDayShift(timestamp, wasPositive = true)
                prefsInteractor.setUntrackedRangeEnd(newValue)
                updateUntrackedRangeViewData()
            }
        }
    }

    fun onThemeChanged() {
        (themeChanged as MutableLiveData).value = false
    }

    fun onAutomatedTrackingHelpClick() {
        router.navigate(
            settingsMapper.toAutomatedTrackingHelpDialog()
        )
    }

    fun onTabReselected(tab: NavigationTab?) {
        if (tab is NavigationTab.Settings) {
            resetScreen.set(Unit)
        }
    }

    private fun openDateTimeDialog(
        tag: String,
        timestamp: Long,
        useMilitaryTime: Boolean,
    ) {
        DateTimeDialogParams(
            tag = tag,
            type = DateTimeDialogType.TIME,
            timestamp = timestamp.let(settingsMapper::startOfDayShiftToTimeStamp),
            useMilitaryTime = useMilitaryTime,
        ).let(router::navigate)
    }

    private fun openCardOrderDialog(cardOrder: CardOrder) {
        router.navigate(
            CardOrderDialogParams(cardOrder)
        )
    }

    private fun showMessage(stringResId: Int) {
        val params = SnackBarParams(message = resourceRepo.getString(stringResId))
        router.show(params)
    }

    private suspend fun updateDaysInCalendarViewData() {
        val data = loadDaysInCalendarViewData()
        daysInCalendarViewData.set(data)
    }

    private suspend fun loadDaysInCalendarViewData(): DaysInCalendarViewData {
        return prefsInteractor.getDaysInCalendar()
            .let(settingsMapper::toDaysInCalendarViewData)
    }

    private suspend fun updateCardOrderViewData() {
        val data = loadCardOrderViewData()
        (cardOrderViewData as MutableLiveData).value = data
    }

    private suspend fun loadCardOrderViewData(): CardOrderViewData {
        return prefsInteractor.getCardOrder()
            .let(settingsMapper::toCardOrderViewData)
    }

    private suspend fun updateFirstDayOfWeekViewData() {
        val data = loadFirstDayOfWeekViewData()
        (firstDayOfWeekViewData as MutableLiveData).value = data
    }

    private suspend fun loadFirstDayOfWeekViewData(): FirstDayOfWeekViewData {
        return prefsInteractor.getFirstDayOfWeek()
            .let(settingsMapper::toFirstDayOfWeekViewData)
    }

    private suspend fun updateStartOfDayViewData() {
        val data = loadStartOfDayViewData()
        startOfDayViewData.set(data)
    }

    private suspend fun loadStartOfDayViewData(): SettingsStartOfDayViewData {
        val shift = prefsInteractor.getStartOfDayShift()

        return SettingsStartOfDayViewData(
            startOfDayValue = settingsMapper.toStartOfDayText(shift, useMilitaryTime = true),
            startOfDaySign = settingsMapper.toStartOfDaySign(shift)
        )
    }

    private suspend fun updateInactivityReminderViewData() {
        val data = loadInactivityReminderViewData()
        inactivityReminderViewData.set(data)
    }

    private suspend fun loadInactivityReminderViewData(): SettingsDurationViewData {
        return prefsInteractor.getInactivityReminderDuration()
            .let(settingsMapper::toDurationViewData)
    }

    private suspend fun updateInactivityReminderDndStartViewData() {
        val data = loadInactivityReminderDndStartViewData()
        inactivityReminderDndStartViewData.set(data)
    }

    private suspend fun loadInactivityReminderDndStartViewData(): String {
        val shift = prefsInteractor.getInactivityReminderDoNotDisturbStart()
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        return settingsMapper.toStartOfDayText(shift, useMilitaryTime)
    }

    private suspend fun updateInactivityReminderDndEndViewData() {
        val data = loadInactivityReminderDndEndViewData()
        inactivityReminderDndEndViewData.set(data)
    }

    private suspend fun loadInactivityReminderDndEndViewData(): String {
        val shift = prefsInteractor.getInactivityReminderDoNotDisturbEnd()
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        return settingsMapper.toStartOfDayText(shift, useMilitaryTime)
    }

    private suspend fun updateActivityReminderViewData() {
        val data = loadActivityReminderViewData()
        activityReminderViewData.set(data)
    }

    private suspend fun loadActivityReminderViewData(): SettingsDurationViewData {
        return prefsInteractor.getActivityReminderDuration()
            .let(settingsMapper::toDurationViewData)
    }

    private suspend fun updateActivityReminderDndStartViewData() {
        val data = loadActivityReminderDndStartViewData()
        activityReminderDndStartViewData.set(data)
    }

    private suspend fun loadActivityReminderDndStartViewData(): String {
        val shift = prefsInteractor.getActivityReminderDoNotDisturbStart()
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        return settingsMapper.toStartOfDayText(shift, useMilitaryTime)
    }

    private suspend fun updateActivityReminderDndEndViewData() {
        val data = loadActivityReminderDndEndViewData()
        activityReminderDndEndViewData.set(data)
    }

    private suspend fun loadActivityReminderDndEndViewData(): String {
        val shift = prefsInteractor.getActivityReminderDoNotDisturbEnd()
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        return settingsMapper.toStartOfDayText(shift, useMilitaryTime)
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

    private suspend fun updateUseProportionalMinutesViewData() {
        val data = loadUseProportionalMinutesViewData()
        useProportionalMinutesHint.set(data)
    }

    private suspend fun loadUseMilitaryTimeViewData(): String {
        return prefsInteractor.getUseMilitaryTimeFormat()
            .let(settingsMapper::toUseMilitaryTimeHint)
    }

    private suspend fun loadUseProportionalMinutesViewData(): String {
        return prefsInteractor.getUseProportionalMinutes()
            .let(settingsMapper::toUseProportionalMinutesHint)
    }

    private fun loadVersionName(): String {
        return applicationDataProvider.getAppVersion().let {
            if (BuildConfig.DEBUG) {
                "$it ${BuildConfig.BUILD_TYPE}"
            } else {
                it
            }
        }
    }

    companion object {
        private const val INACTIVITY_DURATION_DIALOG_TAG = "inactivity_duration_dialog_tag"
        private const val INACTIVITY_REMINDER_DND_START_DIALOG_TAG = "inactivity_reminder_dnd_start_dialog_tag"
        private const val INACTIVITY_REMINDER_DND_END_DIALOG_TAG = "inactivity_reminder_dnd_end_dialog_tag"
        private const val ACTIVITY_DURATION_DIALOG_TAG = "activity_duration_dialog_tag"
        private const val ACTIVITY_REMINDER_DND_START_DIALOG_TAG = "activity_reminder_dnd_start_dialog_tag"
        private const val ACTIVITY_REMINDER_DND_END_DIALOG_TAG = "activity_reminder_dnd_end_dialog_tag"
        private const val IGNORE_SHORT_RECORDS_DIALOG_TAG = "ignore_short_records_dialog_tag"
        private const val IGNORE_SHORT_UNTRACKED_DIALOG_TAG = "ignore_short_untracked_dialog_tag"
        private const val UNTRACKED_RANGE_START_DIALOG_TAG = "untracked_range_start_dialog_tag"
        private const val UNTRACKED_RANGE_END_DIALOG_TAG = "untracked_range_end_dialog_tag"
        private const val START_OF_DAY_DIALOG_TAG = "start_of_day_dialog_tag"
    }
}
