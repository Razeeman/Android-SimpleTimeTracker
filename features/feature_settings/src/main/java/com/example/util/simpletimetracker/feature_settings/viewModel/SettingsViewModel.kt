package com.example.util.simpletimetracker.feature_settings.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.BaseViewModel
import com.example.util.simpletimetracker.core.base.SingleLiveEvent
import com.example.util.simpletimetracker.core.extension.lazySuspend
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.model.NavigationTab
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.core.viewData.SettingsBlock
import com.example.util.simpletimetracker.feature_settings.mapper.SettingsMapper
import com.example.util.simpletimetracker.feature_settings.viewModel.delegate.SettingsAdditionalViewModelDelegate
import com.example.util.simpletimetracker.feature_settings.viewModel.delegate.SettingsBackupViewModelDelegate
import com.example.util.simpletimetracker.feature_settings.viewModel.delegate.SettingsDisplayViewModelDelegate
import com.example.util.simpletimetracker.feature_settings.viewModel.delegate.SettingsExportViewModelDelegate
import com.example.util.simpletimetracker.feature_settings.viewModel.delegate.SettingsMainViewModelDelegate
import com.example.util.simpletimetracker.feature_settings.viewModel.delegate.SettingsNotificationsViewModelDelegate
import com.example.util.simpletimetracker.feature_settings.viewModel.delegate.SettingsParent
import com.example.util.simpletimetracker.feature_settings.viewModel.delegate.SettingsRatingViewModelDelegate
import com.example.util.simpletimetracker.feature_settings.viewModel.delegate.SettingsTranslatorsViewModelDelegate
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    val mainDelegate: SettingsMainViewModelDelegate,
    val displayDelegate: SettingsDisplayViewModelDelegate,
    private val router: Router,
    private val settingsMapper: SettingsMapper,
    private val ratingDelegate: SettingsRatingViewModelDelegate,
    private val notificationsDelegate: SettingsNotificationsViewModelDelegate,
    private val additionalDelegate: SettingsAdditionalViewModelDelegate,
    private val backupDelegate: SettingsBackupViewModelDelegate,
    private val exportDelegate: SettingsExportViewModelDelegate,
    private val translatorsDelegate: SettingsTranslatorsViewModelDelegate,
) : BaseViewModel(), SettingsParent {

    val content: LiveData<List<ViewHolderType>> by lazySuspend { loadContent() }
    val resetScreen: SingleLiveEvent<Unit> = SingleLiveEvent()

    init {
        mainDelegate.init(this)
        notificationsDelegate.init(this)
        displayDelegate.init(this)
        additionalDelegate.init(this)
        backupDelegate.init(this)
        exportDelegate.init(this)
    }

    override fun onCleared() {
        mainDelegate.clear()
        notificationsDelegate.clear()
        displayDelegate.clear()
        additionalDelegate.clear()
        ratingDelegate.clear()
        translatorsDelegate.clear()
        backupDelegate.clear()
        exportDelegate.clear()
        super.onCleared()
    }

    fun onVisible() {
        // Update can come from quick settings widget.
        // Update can come from system settings.
        // Need to update card order because it changes on card order dialog.
        // Update after day changes.
        viewModelScope.launch { updateContent() }
    }

    fun onRequestUpdate() {
        viewModelScope.launch { updateContent() }
    }

    fun onBlockClicked(block: SettingsBlock) {
        when (block) {
            SettingsBlock.NotificationsCollapse ->
                notificationsDelegate.onCollapseClick()
            SettingsBlock.DisplayCollapse ->
                displayDelegate.onCollapseClick()
            SettingsBlock.AdditionalCollapse ->
                additionalDelegate.onCollapseClick()
            SettingsBlock.BackupCollapse ->
                backupDelegate.onCollapseClick()
            SettingsBlock.ExportCollapse ->
                exportDelegate.onCollapseClick()
            SettingsBlock.NotificationsInactivity ->
                notificationsDelegate.onInactivityReminderClicked()
            SettingsBlock.NotificationsActivity ->
                notificationsDelegate.onActivityReminderClicked()
            SettingsBlock.DisplayUntrackedIgnoreShort ->
                displayDelegate.onIgnoreShortUntrackedClicked()
            SettingsBlock.AdditionalIgnoreShort ->
                additionalDelegate.onIgnoreShortRecordsClicked()
            SettingsBlock.AdditionalShiftStartOfDay ->
                additionalDelegate.onStartOfDayClicked()
            SettingsBlock.NotificationsInactivityDoNotDisturbStart ->
                notificationsDelegate.onInactivityReminderDoNotDisturbStartClicked()
            SettingsBlock.NotificationsInactivityDoNotDisturbEnd ->
                notificationsDelegate.onInactivityReminderDoNotDisturbEndClicked()
            SettingsBlock.NotificationsActivityDoNotDisturbStart ->
                notificationsDelegate.onActivityReminderDoNotDisturbStartClicked()
            SettingsBlock.NotificationsActivityDoNotDisturbEnd ->
                notificationsDelegate.onActivityReminderDoNotDisturbEndClicked()
            SettingsBlock.DisplayUntrackedRangeStart ->
                displayDelegate.onUntrackedRangeStartClicked()
            SettingsBlock.DisplayUntrackedRangeEnd ->
                displayDelegate.onUntrackedRangeEndClicked()
            SettingsBlock.Categories ->
                mainDelegate.onEditCategoriesClick()
            SettingsBlock.Archive ->
                mainDelegate.onArchiveClick()
            SettingsBlock.DataEdit ->
                mainDelegate.onDataEditClick()
            SettingsBlock.RateUs ->
                ratingDelegate.onRateClick()
            SettingsBlock.Feedback ->
                ratingDelegate.onFeedbackClick()
            SettingsBlock.DisplayCardSize ->
                displayDelegate.onChangeCardSizeClick()
            SettingsBlock.DisplaySortActivities ->
                displayDelegate.onCardOrderManualClick()
            SettingsBlock.AdditionalShiftStartOfDayButton ->
                additionalDelegate.onStartOfDaySignClicked()
            SettingsBlock.AdditionalAutomatedTracking ->
                additionalDelegate.onAutomatedTrackingHelpClick()
            SettingsBlock.AllowMultitasking ->
                mainDelegate.onAllowMultitaskingClicked()
            SettingsBlock.NotificationsShow ->
                notificationsDelegate.onShowNotificationsClicked()
            SettingsBlock.NotificationsShowControls ->
                notificationsDelegate.onShowNotificationsControlsClicked()
            SettingsBlock.NotificationsInactivityRecurrent ->
                notificationsDelegate.onInactivityReminderRecurrentClicked()
            SettingsBlock.NotificationsActivityRecurrent ->
                notificationsDelegate.onActivityReminderRecurrentClicked()
            SettingsBlock.DisplayUntrackedInRecords ->
                displayDelegate.onShowUntrackedInRecordsClicked()
            SettingsBlock.DisplayUntrackedInStatistics ->
                displayDelegate.onShowUntrackedInStatisticsClicked()
            SettingsBlock.DisplayUntrackedRangeCheckbox ->
                displayDelegate.onUntrackedRangeClicked()
            SettingsBlock.DisplayCalendarView ->
                displayDelegate.onShowRecordsCalendarClicked()
            SettingsBlock.DisplayReverseOrder ->
                displayDelegate.onReverseOrderInCalendarClicked()
            SettingsBlock.DisplayShowActivityFilters ->
                displayDelegate.onShowActivityFiltersClicked()
            SettingsBlock.DisplayGoalsOnSeparateTabs ->
                displayDelegate.onShowGoalsSeparatelyClicked()
            SettingsBlock.DisplayKeepScreenOn ->
                displayDelegate.onKeepScreenOnClicked()
            SettingsBlock.DisplayMilitaryFormat ->
                displayDelegate.onUseMilitaryTimeClicked()
            SettingsBlock.DisplayMonthDayFormat ->
                displayDelegate.onUseMonthDayTimeClicked()
            SettingsBlock.DisplayProportionalFormat ->
                displayDelegate.onUseProportionalMinutesClicked()
            SettingsBlock.DisplayShowSeconds ->
                displayDelegate.onShowSecondsClicked()
            SettingsBlock.AdditionalShowTagSelection ->
                additionalDelegate.onShowRecordTagSelectionClicked()
            SettingsBlock.AdditionalCloseAfterOneTag ->
                additionalDelegate.onRecordTagSelectionCloseClicked()
            SettingsBlock.AdditionalShowWithOnlyGeneral ->
                additionalDelegate.onRecordTagSelectionGeneralClicked()
            SettingsBlock.AdditionalKeepStatisticsRange ->
                additionalDelegate.onKeepStatisticsRangeClicked()
            SettingsBlock.AdditionalSendEvents ->
                additionalDelegate.onAutomatedTrackingSendEventsClicked()
            else -> {
                // Do nothing
            }
        }
    }

    fun onSpinnerPositionSelected(block: SettingsBlock, position: Int) {
        when (block) {
            SettingsBlock.DarkMode ->
                mainDelegate.onDarkModeSelected(position)
            SettingsBlock.Language ->
                mainDelegate.onLanguageSelected(position)
            SettingsBlock.DisplayDaysInCalendar ->
                displayDelegate.onDaysInCalendarSelected(position)
            SettingsBlock.DisplayWidgetBackground ->
                displayDelegate.onWidgetTransparencySelected(position)
            SettingsBlock.DisplaySortActivities ->
                displayDelegate.onRecordTypeOrderSelected(position)
            SettingsBlock.AdditionalFirstDayOfWeek ->
                additionalDelegate.onFirstDayOfWeekSelected(position)
            SettingsBlock.AdditionalRepeatButton ->
                additionalDelegate.onRepeatButtonSelected(position)
            else -> {
                // Do nothing
            }
        }
    }

    fun onDurationSet(tag: String?, duration: Long) {
        when (tag) {
            INACTIVITY_DURATION_DIALOG_TAG,
            ACTIVITY_DURATION_DIALOG_TAG,
            -> notificationsDelegate.onDurationSet(tag, duration)
            IGNORE_SHORT_RECORDS_DIALOG_TAG,
            -> additionalDelegate.onDurationSet(tag, duration)
            IGNORE_SHORT_UNTRACKED_DIALOG_TAG,
            -> displayDelegate.onDurationSet(tag, duration)
        }
    }

    fun onDurationDisabled(tag: String?) {
        when (tag) {
            INACTIVITY_DURATION_DIALOG_TAG,
            ACTIVITY_DURATION_DIALOG_TAG,
            -> notificationsDelegate.onDurationDisabled(tag)
            IGNORE_SHORT_RECORDS_DIALOG_TAG,
            -> additionalDelegate.onDurationDisabled(tag)
            IGNORE_SHORT_UNTRACKED_DIALOG_TAG,
            -> displayDelegate.onDurationDisabled(tag)
        }
    }

    fun onDateTimeSet(timestamp: Long, tag: String?) = viewModelScope.launch {
        when (tag) {
            START_OF_DAY_DIALOG_TAG,
            -> additionalDelegate.onDateTimeSet(timestamp, tag)
            INACTIVITY_REMINDER_DND_START_DIALOG_TAG,
            INACTIVITY_REMINDER_DND_END_DIALOG_TAG,
            ACTIVITY_REMINDER_DND_START_DIALOG_TAG,
            ACTIVITY_REMINDER_DND_END_DIALOG_TAG,
            -> notificationsDelegate.onDateTimeSet(timestamp, tag)
            UNTRACKED_RANGE_START_DIALOG_TAG,
            UNTRACKED_RANGE_END_DIALOG_TAG,
            -> displayDelegate.onDateTimeSet(timestamp, tag)
        }
    }

    fun onTabReselected(tab: NavigationTab?) {
        if (tab is NavigationTab.Settings) {
            resetScreen.set(Unit)
        }
    }

    override fun openDateTimeDialog(
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

    override suspend fun updateContent() {
        content.set(loadContent())
    }

    private suspend fun loadContent(): List<ViewHolderType> {
        val result = mutableListOf<ViewHolderType>()
        result += mainDelegate.getViewData()
        result += ratingDelegate.getViewData()
        result += notificationsDelegate.getViewData()
        result += displayDelegate.getViewData()
        result += additionalDelegate.getViewData()
        result += backupDelegate.getViewData()
        result += exportDelegate.getViewData()
        result += translatorsDelegate.getViewData()
        return result
    }

    companion object {
        const val INACTIVITY_DURATION_DIALOG_TAG = "inactivity_duration_dialog_tag"
        const val INACTIVITY_REMINDER_DND_START_DIALOG_TAG = "inactivity_reminder_dnd_start_dialog_tag"
        const val INACTIVITY_REMINDER_DND_END_DIALOG_TAG = "inactivity_reminder_dnd_end_dialog_tag"
        const val ACTIVITY_DURATION_DIALOG_TAG = "activity_duration_dialog_tag"
        const val ACTIVITY_REMINDER_DND_START_DIALOG_TAG = "activity_reminder_dnd_start_dialog_tag"
        const val ACTIVITY_REMINDER_DND_END_DIALOG_TAG = "activity_reminder_dnd_end_dialog_tag"
        const val IGNORE_SHORT_RECORDS_DIALOG_TAG = "ignore_short_records_dialog_tag"
        const val IGNORE_SHORT_UNTRACKED_DIALOG_TAG = "ignore_short_untracked_dialog_tag"
        const val UNTRACKED_RANGE_START_DIALOG_TAG = "untracked_range_start_dialog_tag"
        const val UNTRACKED_RANGE_END_DIALOG_TAG = "untracked_range_end_dialog_tag"
        const val START_OF_DAY_DIALOG_TAG = "start_of_day_dialog_tag"
    }
}
