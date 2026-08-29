package com.example.util.simpletimetracker.feature_settings.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.BaseViewModel
import com.example.util.simpletimetracker.core.base.SingleLiveEvent
import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.core.extension.lazySuspend
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.model.NavigationTab
import com.example.util.simpletimetracker.domain.darkMode.interactor.ThemeChangedInteractor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.dayOfWeek.DayOfWeekViewData
import com.example.util.simpletimetracker.feature_settings.api.SettingsBlock
import com.example.util.simpletimetracker.domain.statistics.interactor.SettingsDataUpdateInteractor
import com.example.util.simpletimetracker.feature_settings.mapper.SettingsMapper
import com.example.util.simpletimetracker.feature_settings.model.OptionsContent
import com.example.util.simpletimetracker.feature_settings.viewModel.delegate.SettingsAdditionalViewModelDelegate
import com.example.util.simpletimetracker.feature_settings.viewModel.delegate.SettingsBackupViewModelDelegate
import com.example.util.simpletimetracker.feature_settings.viewModel.delegate.SettingsContributorsViewModelDelegate
import com.example.util.simpletimetracker.feature_settings.viewModel.delegate.SettingsDelegate
import com.example.util.simpletimetracker.feature_settings.viewModel.delegate.SettingsDisplayViewModelDelegate
import com.example.util.simpletimetracker.feature_settings.viewModel.delegate.SettingsExportViewModelDelegate
import com.example.util.simpletimetracker.feature_settings.viewModel.delegate.SettingsMainViewModelDelegate
import com.example.util.simpletimetracker.feature_settings.viewModel.delegate.SettingsNotificationsViewModelDelegate
import com.example.util.simpletimetracker.feature_settings.viewModel.delegate.SettingsParent
import com.example.util.simpletimetracker.feature_settings.viewModel.delegate.SettingsRatingViewModelDelegate
import com.example.util.simpletimetracker.feature_settings.viewModel.delegate.SettingsTranslatorsViewModelDelegate
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.DataExportSettingsResult
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogType
import com.example.util.simpletimetracker.navigation.params.screen.OptionsListParams
import com.example.util.simpletimetracker.navigation.params.screen.SettingsOptionsParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    ratingDelegate: SettingsRatingViewModelDelegate,
    notificationsDelegate: SettingsNotificationsViewModelDelegate,
    displayDelegate: SettingsDisplayViewModelDelegate,
    additionalDelegate: SettingsAdditionalViewModelDelegate,
    mainDelegate: SettingsMainViewModelDelegate,
    backupDelegate: SettingsBackupViewModelDelegate,
    exportDelegate: SettingsExportViewModelDelegate,
    translatorsDelegate: SettingsTranslatorsViewModelDelegate,
    contributorsDelegate: SettingsContributorsViewModelDelegate,
    private val router: Router,
    private val settingsMapper: SettingsMapper,
    private val settingsDataUpdateInteractor: SettingsDataUpdateInteractor,
    private val themeChangedInteractor: ThemeChangedInteractor,
) : BaseViewModel(), SettingsParent {

    val content: LiveData<List<ViewHolderType>> by lazySuspend { loadContent() }
    val optionsContent: LiveData<List<ViewHolderType>> = MutableLiveData()
    val resetScreen: SingleLiveEvent<Unit> = SingleLiveEvent()
    val keepScreenOnCheckbox: LiveData<Boolean> by additionalDelegate::keepScreenOnCheckbox
    val themeChanged: SingleLiveEvent<Boolean> by mainDelegate::themeChanged

    private val delegates: List<SettingsDelegate> = listOf(
        mainDelegate,
        ratingDelegate,
        notificationsDelegate,
        displayDelegate,
        additionalDelegate,
        backupDelegate,
        exportDelegate,
        translatorsDelegate,
        contributorsDelegate,
    )

    private var activeOptionsContent: OptionsContent? = null

    init {
        delegates.forEach { it.init(this) }
        additionalDelegate.init(this)
        backupDelegate.init(this)
        exportDelegate.init(this)
        subscribeToUpdates()
    }

    override fun onCleared() {
        delegates.forEach { (it as? ViewModelDelegate)?.clear() }
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
        delegates.forEach { it.onHidden() }
    }

    fun onBlockClicked(block: SettingsBlock) {
        delegates.forEach { it.onBlockClicked(block) }
    }

    fun onSpinnerPositionSelected(block: SettingsBlock, position: Int) {
        delegates.forEach { it.onSpinnerPositionSelected(block, position) }
    }

    fun onPositiveClick(tag: String?) {
        delegates.forEach { it.onPositiveClick(tag) }
    }

    fun onDurationSet(tag: String?, duration: Long) {
        delegates.forEach { it.onDurationSet(tag, duration) }
    }

    fun onDurationDisabled(tag: String?) {
        delegates.forEach { it.onDurationDisabled(tag) }
    }

    fun onDateTimeSet(timestamp: Long, tag: String?) {
        delegates.forEach { it.onDateTimeSet(timestamp, tag) }
    }

    fun onDataExportSettingsSelected(data: DataExportSettingsResult) {
        delegates.forEach { it.onDataExportSettingsSelected(data) }
    }

    fun onTypesSelected(typeIds: List<Long>, tag: String) {
        delegates.forEach { it.onTypesSelected(typeIds, tag) }
    }

    fun onOptionsItemClick(id: OptionsListParams.Item.Id) {
        delegates.forEach { it.onOptionsItemClick(id) }
    }

    fun onDayOfWeekClicked(block: SettingsBlock, data: DayOfWeekViewData) {
        delegates.forEach { it.onDayOfWeekClicked(block, data) }
    }

    fun onTabReselected(tab: NavigationTab?) {
        if (tab is NavigationTab.Settings) {
            resetScreen.set(Unit)
        }
    }

    fun onResetScreen() = viewModelScope.launch {
        delegates.forEach { it.collapse() }
        updateContent()
    }

    fun onThemeChanged() = viewModelScope.launch {
        themeChangedInteractor.send()
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
        optionsContent.set(loadOptionsContent())
    }

    override fun openOptions(content: OptionsContent) {
        viewModelScope.launch {
            activeOptionsContent = content
            optionsContent.set(loadOptionsContent())
            router.navigate(SettingsOptionsParams)
        }
    }

    private fun subscribeToUpdates() = viewModelScope.launch {
        settingsDataUpdateInteractor.dataUpdated.collect {
            updateContent()
        }
    }

    private suspend fun loadContent(): List<ViewHolderType> {
        val order: List<SettingsDelegate.Key> = listOf(
            SettingsMainViewModelDelegate,
            SettingsRatingViewModelDelegate,
            SettingsNotificationsViewModelDelegate,
            SettingsDisplayViewModelDelegate,
            SettingsAdditionalViewModelDelegate,
            SettingsBackupViewModelDelegate,
            SettingsExportViewModelDelegate,
            SettingsTranslatorsViewModelDelegate,
            SettingsContributorsViewModelDelegate,
        )
        val viewData = delegates.map { it.getViewData() }.associateBy { it.key }
        return order.mapNotNull { key -> viewData[key]?.data }.flatten()
    }

    private suspend fun loadOptionsContent(): List<ViewHolderType> {
        val content = activeOptionsContent ?: return emptyList()
        return delegates.map { it.getSheetViewData(content) }.firstOrNull { !it.isNullOrEmpty() }.orEmpty()
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
        const val AUTO_BACKUP_TRIGGER_TIME_DIALOG_TAG = "auto_backup_trigger_time_dialog_tag"
        const val AUTO_EXPORT_TRIGGER_TIME_DIALOG_TAG = "auto_export_trigger_time_dialog_tag"
        const val TAG_EXCLUDE_ACTIVITIES_TYPES_SELECTION = "tag_exclude_activities_types_selection"
        const val COMMENT_EXCLUDE_ACTIVITIES_TYPES_SELECTION = "comment_exclude_activities_types_selection"
        const val CLOSE_AFTER_ONE_TAG_EXCLUDE_ACTIVITIES_TYPES_SELECTION = "close_after_one_exclude_activities"
        const val SELECT_ACTIVITIES_TO_AUTOSTART_POMODORO = "select_activities_to_autostart_pomodoro"
    }
}
