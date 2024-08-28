package com.example.util.simpletimetracker.feature_settings.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.BaseViewModel
import com.example.util.simpletimetracker.core.base.SingleLiveEvent
import com.example.util.simpletimetracker.core.extension.lazySuspend
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.model.NavigationTab
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_settings_views.SettingsBlock
import com.example.util.simpletimetracker.domain.interactor.SettingsDataUpdateInteractor
import com.example.util.simpletimetracker.feature_settings.mapper.SettingsMapper
import com.example.util.simpletimetracker.feature_settings.viewModel.delegate.SettingsAdditionalViewModelDelegate
import com.example.util.simpletimetracker.feature_settings.viewModel.delegate.SettingsBackupViewModelDelegate
import com.example.util.simpletimetracker.feature_settings.viewModel.delegate.SettingsContributorsViewModelDelegate
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
    private val mainDelegate: SettingsMainViewModelDelegate,
    private val displayDelegate: SettingsDisplayViewModelDelegate,
    private val router: Router,
    private val settingsMapper: SettingsMapper,
    private val ratingDelegate: SettingsRatingViewModelDelegate,
    private val notificationsDelegate: SettingsNotificationsViewModelDelegate,
    private val additionalDelegate: SettingsAdditionalViewModelDelegate,
    private val backupDelegate: SettingsBackupViewModelDelegate,
    private val exportDelegate: SettingsExportViewModelDelegate,
    private val translatorsDelegate: SettingsTranslatorsViewModelDelegate,
    private val contributorsDelegate: SettingsContributorsViewModelDelegate,
    private val settingsDataUpdateInteractor: SettingsDataUpdateInteractor,
) : BaseViewModel(), SettingsParent {

    val content: LiveData<List<ViewHolderType>> by lazySuspend { loadContent() }
    val resetScreen: SingleLiveEvent<Unit> = SingleLiveEvent()
    val keepScreenOnCheckbox: LiveData<Boolean> by additionalDelegate::keepScreenOnCheckbox
    val themeChanged: SingleLiveEvent<Boolean> by mainDelegate::themeChanged

    init {
        mainDelegate.init(this)
        notificationsDelegate.init(this)
        displayDelegate.init(this)
        additionalDelegate.init(this)
        backupDelegate.init(this)
        exportDelegate.init(this)
        ratingDelegate.init(this)
        subscribeToUpdates()
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

    fun onHidden() {
        ratingDelegate.onHidden()
    }

    fun onRequestUpdate() {
        viewModelScope.launch { updateContent() }
    }

    fun onBlockClicked(block: SettingsBlock) {
        mainDelegate.onBlockClicked(block)
        notificationsDelegate.onBlockClicked(block)
        displayDelegate.onBlockClicked(block)
        additionalDelegate.onBlockClicked(block)
        backupDelegate.onBlockClicked(block)
        exportDelegate.onBlockClicked(block)
        ratingDelegate.onBlockClicked(block)
    }

    fun onSpinnerPositionSelected(block: SettingsBlock, position: Int) {
        displayDelegate.onSpinnerPositionSelected(block, position)
        additionalDelegate.onSpinnerPositionSelected(block, position)
        mainDelegate.onSpinnerPositionSelected(block, position)
    }

    fun onDurationSet(tag: String?, duration: Long) {
        notificationsDelegate.onDurationSet(tag, duration)
        displayDelegate.onDurationSet(tag, duration)
        additionalDelegate.onDurationSet(tag, duration)
    }

    fun onDurationDisabled(tag: String?) {
        notificationsDelegate.onDurationDisabled(tag)
        displayDelegate.onDurationDisabled(tag)
        additionalDelegate.onDurationDisabled(tag)
    }

    fun onDateTimeSet(timestamp: Long, tag: String?) {
        notificationsDelegate.onDateTimeSet(timestamp, tag)
        displayDelegate.onDateTimeSet(timestamp, tag)
        additionalDelegate.onDateTimeSet(timestamp, tag)
    }

    fun onTypesSelected(
        typeIds: List<Long>,
        tag: String?,
    ) {
        displayDelegate.onTypesSelected(typeIds, tag)
        additionalDelegate.onTypesSelected(typeIds, tag)
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

    private fun subscribeToUpdates() = viewModelScope.launch {
        settingsDataUpdateInteractor.dataUpdated.collect {
            updateContent()
        }
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
        result += contributorsDelegate.getViewData()
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
        const val EXCLUDE_ACTIVITIES_TYPES_SELECTION = "exclude_activities_types_selection"
        const val SELECT_ACTIVITIES_TO_AUTOSTART_POMODORO = "select_activities_to_autostart_pomodoro"
    }
}
