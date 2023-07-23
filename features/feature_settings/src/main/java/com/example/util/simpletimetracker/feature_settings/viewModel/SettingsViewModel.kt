package com.example.util.simpletimetracker.feature_settings.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.BaseViewModel
import com.example.util.simpletimetracker.core.base.SingleLiveEvent
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.model.NavigationTab
import com.example.util.simpletimetracker.domain.extension.flip
import com.example.util.simpletimetracker.domain.extension.orFalse
import com.example.util.simpletimetracker.feature_settings.mapper.SettingsMapper
import com.example.util.simpletimetracker.feature_settings.viewModel.delegate.SettingsAdditionalViewModelDelegate
import com.example.util.simpletimetracker.feature_settings.viewModel.delegate.SettingsDisplayViewModelDelegate
import com.example.util.simpletimetracker.feature_settings.viewModel.delegate.SettingsMainViewModelDelegate
import com.example.util.simpletimetracker.feature_settings.viewModel.delegate.SettingsNotificationsViewModelDelegate
import com.example.util.simpletimetracker.feature_settings.viewModel.delegate.SettingsParent
import com.example.util.simpletimetracker.feature_settings.viewModel.delegate.SettingsRatingViewModelDelegate
import com.example.util.simpletimetracker.feature_settings.viewModel.delegate.SettingsTranslatorsViewModelDelegate
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.ArchiveParams
import com.example.util.simpletimetracker.navigation.params.screen.CategoriesParams
import com.example.util.simpletimetracker.navigation.params.screen.DataEditParams
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    val mainDelegate: SettingsMainViewModelDelegate,
    val notificationsDelegate: SettingsNotificationsViewModelDelegate,
    val displayDelegate: SettingsDisplayViewModelDelegate,
    val additionalDelegate: SettingsAdditionalViewModelDelegate,
    val ratingDelegate: SettingsRatingViewModelDelegate,
    val translatorsDelegate: SettingsTranslatorsViewModelDelegate,
    private val router: Router,
    private val settingsMapper: SettingsMapper,
) : BaseViewModel(), SettingsParent {

    val settingsNotificationsVisibility: LiveData<Boolean> = MutableLiveData(false)
    val settingsDisplayVisibility: LiveData<Boolean> = MutableLiveData(false)
    val settingsAdditionalVisibility: LiveData<Boolean> = MutableLiveData(false)
    val settingsBackupVisibility: LiveData<Boolean> = MutableLiveData(false)
    val resetScreen: SingleLiveEvent<Unit> = SingleLiveEvent()

    init {
        notificationsDelegate.init(this)
        displayDelegate.init(this)
        additionalDelegate.init(this)
    }

    override fun onCleared() {
        mainDelegate.clear()
        notificationsDelegate.clear()
        displayDelegate.clear()
        additionalDelegate.clear()
        ratingDelegate.clear()
        translatorsDelegate.clear()
        super.onCleared()
    }

    override suspend fun onUseMilitaryTimeClicked() {
        additionalDelegate.onUseMilitaryTimeClicked()
        notificationsDelegate.onUseMilitaryTimeClicked()
    }

    fun onVisible() {
        mainDelegate.onVisible()
        displayDelegate.onVisible()
        additionalDelegate.onVisible()
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
