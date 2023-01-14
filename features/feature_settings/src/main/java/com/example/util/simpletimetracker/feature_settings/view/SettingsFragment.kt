package com.example.util.simpletimetracker.feature_settings.view

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.dialog.DataExportSettingsDialogListener
import com.example.util.simpletimetracker.core.dialog.DateTimeDialogListener
import com.example.util.simpletimetracker.core.dialog.DurationDialogListener
import com.example.util.simpletimetracker.core.dialog.StandardDialogListener
import com.example.util.simpletimetracker.core.sharedViewModel.BackupViewModel
import com.example.util.simpletimetracker.feature_settings.viewData.CardOrderViewData
import com.example.util.simpletimetracker.feature_settings.viewData.FirstDayOfWeekViewData
import com.example.util.simpletimetracker.feature_settings.viewData.SettingsStartOfDayViewData
import com.example.util.simpletimetracker.feature_settings.viewModel.SettingsViewModel
import com.example.util.simpletimetracker.feature_views.extension.rotateDown
import com.example.util.simpletimetracker.feature_views.extension.rotateUp
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.navigation.params.screen.DataExportSettingsResult
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.example.util.simpletimetracker.feature_settings.databinding.SettingsFragmentBinding as Binding
import androidx.core.view.isVisible
import com.example.util.simpletimetracker.feature_settings.viewData.SettingsDurationViewData

@AndroidEntryPoint
class SettingsFragment :
    BaseFragment<Binding>(),
    StandardDialogListener,
    DurationDialogListener,
    DateTimeDialogListener,
    DataExportSettingsDialogListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory<SettingsViewModel>

    @Inject
    lateinit var backupViewModelFactory: BaseViewModelFactory<BackupViewModel>

    private val viewModel: SettingsViewModel by viewModels(
        factoryProducer = { viewModelFactory }
    )
    private val backupViewModel: BackupViewModel by viewModels(
        ownerProducer = { activity as AppCompatActivity },
        factoryProducer = { backupViewModelFactory }
    )

    override fun initUi() = with(binding) {
        with(layoutSettingsDisplay) {
            spinnerSettingsRecordTypeSort.setProcessSameItemSelection(false)
        }
        with(layoutSettingsAdditional) {
            spinnerSettingsFirstDayOfWeek.setProcessSameItemSelection(false)
        }
    }

    override fun initUx() = with(binding) {
        with(layoutSettingsMain) {
            checkboxSettingsAllowMultitasking.setOnClick(viewModel::onAllowMultitaskingClicked)
            checkboxSettingsShowNotifications.setOnClick(viewModel::onShowNotificationsClicked)
            checkboxSettingsDarkMode.setOnClick(viewModel::onDarkModeClicked)
            layoutSettingsEditCategories.setOnClick(viewModel::onEditCategoriesClick)
            tvSettingsArchive.setOnClick(viewModel::onArchiveClick)
        }
        with(layoutSettingsDisplay) {
            layoutSettingsDisplayTitle.setOnClick(viewModel::onSettingsDisplayClick)
            spinnerSettingsRecordTypeSort.onPositionSelected = viewModel::onRecordTypeOrderSelected
            btnCardOrderManual.setOnClick(viewModel::onCardOrderManualClick)
            checkboxSettingsShowUntracked.setOnClick(viewModel::onShowUntrackedClicked)
            checkboxSettingsShowRecordsCalendar.setOnClick(viewModel::onShowRecordsCalendarClicked)
            checkboxSettingsReverseOrderInCalendar.setOnClick(viewModel::onReverseOrderInCalendarClicked)
            checkboxSettingsShowActivityFilters.setOnClick(viewModel::onShowActivityFiltersClicked)
            checkboxSettingsUseMilitaryTime.setOnClick(viewModel::onUseMilitaryTimeClicked)
            checkboxSettingsUseProportionalMinutes.setOnClick(viewModel::onUseProportionalMinutesClicked)
            checkboxSettingsShowSeconds.setOnClick(viewModel::onShowSecondsClicked)
            checkboxSettingsKeepScreenOn.setOnClick(viewModel::onKeepScreenOnClicked)
            tvSettingsChangeCardSize.setOnClick(viewModel::onChangeCardSizeClick)
        }
        with(layoutSettingsAdditional) {
            layoutSettingsAdditionalTitle.setOnClick(viewModel::onSettingsAdditionalClick)
            spinnerSettingsFirstDayOfWeek.onPositionSelected = viewModel::onFirstDayOfWeekSelected
            groupSettingsStartOfDay.setOnClick(viewModel::onStartOfDayClicked)
            btnSettingsStartOfDaySign.setOnClick(viewModel::onStartOfDaySignClicked)
            checkboxSettingsKeepStatisticsRange.setOnClick(viewModel::onKeepStatisticsRangeClicked)
            groupSettingsInactivityReminder.setOnClick(viewModel::onInactivityReminderClicked)
            checkboxSettingsInactivityReminderRecurrent.setOnClick(viewModel::onInactivityReminderRecurrentClicked)
            groupSettingsIgnoreShortRecords.setOnClick(viewModel::onIgnoreShortRecordsClicked)
            checkboxSettingsShowRecordTagSelection.setOnClick(viewModel::onShowRecordTagSelectionClicked)
            checkboxSettingsRecordTagSelectionClose.setOnClick(viewModel::onRecordTagSelectionCloseClicked)
            btnSettingsAutomatedTracking.setOnClick(viewModel::onAutomatedTrackingHelpClick)
        }
        with(layoutSettingsRating) {
            layoutSettingsRate.setOnClick(viewModel::onRateClick)
            layoutSettingsFeedback.setOnClick(viewModel::onFeedbackClick)
        }
        with(layoutSettingsBackup) {
            layoutSettingsSaveBackup.setOnClick(backupViewModel::onSaveClick)
            layoutSettingsRestoreBackup.setOnClick(backupViewModel::onRestoreClick)
            layoutSettingsExportCsv.setOnClick(viewModel::onExportCsvClick)
            layoutSettingsExportIcs.setOnClick(viewModel::onExportIcsClick)
        }
    }

    override fun initViewModel(): Unit = with(binding) {
        with(viewModel) {
            with(layoutSettingsMain) {
                allowMultitaskingCheckbox.observe(checkboxSettingsAllowMultitasking::setChecked)
                showNotificationsCheckbox.observe(checkboxSettingsShowNotifications::setChecked)
                darkModeCheckbox.observe(checkboxSettingsDarkMode::setChecked)
            }
            with(layoutSettingsDisplay) {
                settingsDisplayVisibility.observe { opened ->
                    layoutSettingsDisplayContent.visible = opened
                    arrowSettingsDisplay.apply { if (opened) rotateDown() else rotateUp() }
                }
                btnCardOrderManualVisibility.observe(btnCardOrderManual::visible::set)
                showUntrackedCheckbox.observe(checkboxSettingsShowUntracked::setChecked)
                showRecordsCalendarCheckbox.observe(::updateShowRecordCalendarChecked)
                reverseOrderInCalendarCheckbox.observe(checkboxSettingsReverseOrderInCalendar::setChecked)
                showActivityFiltersCheckbox.observe(checkboxSettingsShowActivityFilters::setChecked)
                useMilitaryTimeCheckbox.observe(checkboxSettingsUseMilitaryTime::setChecked)
                useProportionalMinutesCheckbox.observe(checkboxSettingsUseProportionalMinutes::setChecked)
                showSecondsCheckbox.observe(checkboxSettingsShowSeconds::setChecked)
                useMilitaryTimeHint.observe(tvSettingsUseMilitaryTimeHint::setText)
                useProportionalMinutesHint.observe(tvSettingsUseProportionalMinutesHint::setText)
            }
            with(layoutSettingsAdditional) {
                settingsAdditionalVisibility.observe { opened ->
                    layoutSettingsAdditionalContent.visible = opened
                    arrowSettingsAdditional.apply { if (opened) rotateDown() else rotateUp() }
                }
                keepStatisticsRangeCheckbox.observe(checkboxSettingsKeepStatisticsRange::setChecked)
                inactivityReminderViewData.observe(::updateInactivityReminder)
                inactivityReminderRecurrentCheckbox.observe(checkboxSettingsInactivityReminderRecurrent::setChecked)
                ignoreShortRecordsViewData.observe(tvSettingsIgnoreShortRecordsTime::setText)
                recordTagSelectionCloseCheckbox.observe(checkboxSettingsRecordTagSelectionClose::setChecked)
            }
            with(layoutSettingsRating) {
                versionName.observe(tvSettingsVersionName::setText)
            }
            cardOrderViewData.observe(::updateCardOrderViewData)
            firstDayOfWeekViewData.observe(::updateFirstDayOfWeekViewData)
            startOfDayViewData.observe(::updateStartOfDayViewData)
            keepScreenOnCheckbox.observe(::setKeepScreenOn)
            showRecordTagSelectionCheckbox.observe(::updateShowRecordTagSelectionChecked)
            themeChanged.observe(::changeTheme)
        }
    }

    override fun onResume() = with(binding) {
        super.onResume()
        with(layoutSettingsMain) {
            checkboxSettingsAllowMultitasking.jumpDrawablesToCurrentState()
            checkboxSettingsShowNotifications.jumpDrawablesToCurrentState()
            checkboxSettingsDarkMode.jumpDrawablesToCurrentState()
        }
        with(layoutSettingsDisplay) {
            spinnerSettingsRecordTypeSort.jumpDrawablesToCurrentState()
            checkboxSettingsShowUntracked.jumpDrawablesToCurrentState()
            checkboxSettingsShowRecordsCalendar.jumpDrawablesToCurrentState()
            checkboxSettingsReverseOrderInCalendar.jumpDrawablesToCurrentState()
            checkboxSettingsShowActivityFilters.jumpDrawablesToCurrentState()
            checkboxSettingsUseMilitaryTime.jumpDrawablesToCurrentState()
            checkboxSettingsUseProportionalMinutes.jumpDrawablesToCurrentState()
            checkboxSettingsShowSeconds.jumpDrawablesToCurrentState()
            checkboxSettingsKeepScreenOn.jumpDrawablesToCurrentState()
        }
        with(layoutSettingsAdditional) {
            spinnerSettingsFirstDayOfWeek.jumpDrawablesToCurrentState()
            checkboxSettingsKeepStatisticsRange.jumpDrawablesToCurrentState()
            checkboxSettingsShowRecordTagSelection.jumpDrawablesToCurrentState()
            checkboxSettingsInactivityReminderRecurrent.jumpDrawablesToCurrentState()
            checkboxSettingsRecordTagSelectionClose.jumpDrawablesToCurrentState()
        }
        viewModel.onVisible()
    }

    override fun onPositiveClick(tag: String?, data: Any?) {
        backupViewModel.onPositiveDialogClick(tag)
    }

    override fun onDurationSet(duration: Long, tag: String?) {
        viewModel.onDurationSet(tag, duration)
    }

    override fun onDisable(tag: String?) {
        viewModel.onDurationDisabled(tag)
    }

    override fun onDateTimeSet(timestamp: Long, tag: String?) {
        viewModel.onDateTimeSet(timestamp, tag)
    }

    override fun onDataExportSettingsSelected(data: DataExportSettingsResult) {
        backupViewModel.onDataExportSettingsSelected(data)
    }

    private fun updateCardOrderViewData(
        viewData: CardOrderViewData,
    ) = with(binding.layoutSettingsDisplay) {
        btnCardOrderManual.visible = viewData.isManualConfigButtonVisible
        spinnerSettingsRecordTypeSort.setData(viewData.items, viewData.selectedPosition)
        tvSettingsRecordTypeSortValue.text = viewData.items
            .getOrNull(viewData.selectedPosition)?.text.orEmpty()
    }

    private fun updateFirstDayOfWeekViewData(
        viewData: FirstDayOfWeekViewData,
    ) = with(binding.layoutSettingsAdditional) {
        spinnerSettingsFirstDayOfWeek.setData(viewData.items, viewData.selectedPosition)
        tvSettingsFirstDayOfWeekValue.text = viewData.items
            .getOrNull(viewData.selectedPosition)?.text.orEmpty()
    }

    private fun updateShowRecordTagSelectionChecked(
        isChecked: Boolean,
    ) = with(binding.layoutSettingsAdditional) {
        checkboxSettingsShowRecordTagSelection.isChecked = isChecked
        groupSettingsRecordTagSelectionClose.visible = isChecked
    }

    private fun updateInactivityReminder(
        data: SettingsDurationViewData,
    ) = with(binding.layoutSettingsAdditional) {
        tvSettingsInactivityReminderTime.text = data.text
        groupSettingsInactivityReminderRecurrent.isVisible = data.enabled
    }

    private fun updateShowRecordCalendarChecked(
        isChecked: Boolean,
    ) = with(binding.layoutSettingsDisplay) {
        checkboxSettingsShowRecordsCalendar.isChecked = isChecked
        groupSettingsReverseOrderInCalendar.visible = isChecked
    }

    private fun updateStartOfDayViewData(
        viewData: SettingsStartOfDayViewData,
    ) = with(binding.layoutSettingsAdditional) {
        tvSettingsStartOfDayTime.text = viewData.startOfDayValue
        btnSettingsStartOfDaySign.visible = viewData.startOfDaySign.isNotEmpty()
        tvSettingsStartOfDaySign.text = viewData.startOfDaySign
    }

    private fun setKeepScreenOn(keepScreenOn: Boolean) {
        binding.layoutSettingsDisplay.checkboxSettingsKeepScreenOn.isChecked = keepScreenOn
        if (keepScreenOn) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    private fun changeTheme(themeChanged: Boolean) {
        if (themeChanged) {
            viewModel.onThemeChanged()
            activity?.recreate()
            // TODO fix fade and save scroll
            activity?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    companion object {
        fun newInstance(): SettingsFragment = SettingsFragment()
    }
}
