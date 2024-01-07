package com.example.util.simpletimetracker.feature_settings.view

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.dialog.DataExportSettingsDialogListener
import com.example.util.simpletimetracker.core.dialog.DateTimeDialogListener
import com.example.util.simpletimetracker.core.dialog.DurationDialogListener
import com.example.util.simpletimetracker.core.dialog.StandardDialogListener
import com.example.util.simpletimetracker.core.sharedViewModel.BackupViewModel
import com.example.util.simpletimetracker.core.sharedViewModel.MainTabsViewModel
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_settings.adapter.createSettingsBottomAdapterDelegate
import com.example.util.simpletimetracker.feature_settings.adapter.createSettingsCheckboxAdapterDelegate
import com.example.util.simpletimetracker.feature_settings.adapter.createSettingsSpinnerAdapterDelegate
import com.example.util.simpletimetracker.feature_settings.adapter.createSettingsSpinnerNotCheckableAdapterDelegate
import com.example.util.simpletimetracker.feature_settings.adapter.createSettingsTextAdapterDelegate
import com.example.util.simpletimetracker.feature_settings.adapter.createSettingsTopAdapterDelegate
import com.example.util.simpletimetracker.feature_settings.adapter.createSettingsTranslatorAdapterDelegate
import com.example.util.simpletimetracker.feature_settings.viewData.CardOrderViewData
import com.example.util.simpletimetracker.feature_settings.viewData.DaysInCalendarViewData
import com.example.util.simpletimetracker.feature_settings.viewData.FirstDayOfWeekViewData
import com.example.util.simpletimetracker.feature_settings.viewData.RepeatButtonViewData
import com.example.util.simpletimetracker.feature_settings.viewData.SettingsDurationViewData
import com.example.util.simpletimetracker.feature_settings.viewData.SettingsStartOfDayViewData
import com.example.util.simpletimetracker.feature_settings.viewData.SettingsUntrackedRangeViewData
import com.example.util.simpletimetracker.feature_settings.viewData.WidgetTransparencyViewData
import com.example.util.simpletimetracker.feature_settings.viewModel.SettingsViewModel
import com.example.util.simpletimetracker.feature_views.extension.rotateDown
import com.example.util.simpletimetracker.feature_views.extension.rotateUp
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.navigation.params.screen.DataExportSettingsResult
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.example.util.simpletimetracker.feature_settings.databinding.SettingsFragmentBinding as Binding

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
    lateinit var backupViewModelFactory: BaseViewModelFactory<BackupViewModel>

    @Inject
    lateinit var mainTabsViewModelFactory: BaseViewModelFactory<MainTabsViewModel>

    private val viewModel: SettingsViewModel by viewModels()
    private val backupViewModel: BackupViewModel by activityViewModels { backupViewModelFactory }
    private val mainTabsViewModel: MainTabsViewModel by activityViewModels { mainTabsViewModelFactory }

    private val translatorsAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createSettingsTranslatorAdapterDelegate(),
        )
    }
    private val contentAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createSettingsTopAdapterDelegate(),
            createSettingsBottomAdapterDelegate(),
            createSettingsTextAdapterDelegate(throttle(viewModel::onTextClicked)),
            createSettingsCheckboxAdapterDelegate(viewModel::onCheckboxClicked),
            createSettingsSpinnerAdapterDelegate(viewModel::onSpinnerPositionSelected),
            createSettingsSpinnerNotCheckableAdapterDelegate(viewModel::onSpinnerPositionSelected),
        )
    }

    override fun initUi() = with(binding) {
        rvSettingsContent.adapter = contentAdapter
        layoutSettingsTranslators.rvSettingsTranslators.adapter = translatorsAdapter
        layoutSettingsDisplay.spinnerSettingsDaysInCalendar.setProcessSameItemSelection(false)
        layoutSettingsDisplay.spinnerSettingsWidgetTransparency.setProcessSameItemSelection(false)
        layoutSettingsDisplay.spinnerSettingsRecordTypeSort.setProcessSameItemSelection(false)
        layoutSettingsAdditional.spinnerSettingsFirstDayOfWeek.setProcessSameItemSelection(false)
        layoutSettingsAdditional.spinnerSettingsRepeatButtonType.setProcessSameItemSelection(false)
    }

    override fun initUx() = with(binding) {
        with(layoutSettingsNotifications) {
            with(viewModel.notificationsDelegate) {
                layoutSettingsNotificationsTitle.setOnClick(viewModel::onSettingsNotificationsClick)
                checkboxSettingsShowNotifications.setOnClick(::onShowNotificationsClicked)
                checkboxSettingsShowNotificationsControls.setOnClick(::onShowNotificationsControlsClicked)
                groupSettingsInactivityReminder.setOnClick(::onInactivityReminderClicked)
                checkboxSettingsInactivityReminderRecurrent.setOnClick(::onInactivityReminderRecurrentClicked)
                tvSettingsInactivityReminderDndStart.setOnClick(::onInactivityReminderDoNotDisturbStartClicked)
                tvSettingsInactivityReminderDndEnd.setOnClick(::onInactivityReminderDoNotDisturbEndClicked)
                groupSettingsActivityReminder.setOnClick(::onActivityReminderClicked)
                checkboxSettingsActivityReminderRecurrent.setOnClick(::onActivityReminderRecurrentClicked)
                tvSettingsActivityReminderDndStart.setOnClick(::onActivityReminderDoNotDisturbStartClicked)
                tvSettingsActivityReminderDndEnd.setOnClick(::onActivityReminderDoNotDisturbEndClicked)
            }
        }

        with(layoutSettingsDisplay) {
            with(viewModel.displayDelegate) {
                layoutSettingsDisplayTitle.setOnClick(viewModel::onSettingsDisplayClick)
                spinnerSettingsDaysInCalendar.onPositionSelected = ::onDaysInCalendarSelected
                spinnerSettingsWidgetTransparency.onPositionSelected = ::onWidgetTransparencySelected
                spinnerSettingsRecordTypeSort.onPositionSelected = ::onRecordTypeOrderSelected
                btnCardOrderManual.setOnClick(::onCardOrderManualClick)
                checkboxSettingsShowUntrackedInRecords.setOnClick(::onShowUntrackedInRecordsClicked)
                checkboxSettingsShowUntrackedInStatistics.setOnClick(::onShowUntrackedInStatisticsClicked)
                groupSettingsIgnoreShortUntracked.setOnClick(::onIgnoreShortUntrackedClicked)
                checkboxSettingsUntrackedRange.setOnClick(::onUntrackedRangeClicked)
                tvSettingsUntrackedRangeStart.setOnClick(::onUntrackedRangeStartClicked)
                tvSettingsUntrackedRangeEnd.setOnClick(::onUntrackedRangeEndClicked)
                checkboxSettingsShowRecordsCalendar.setOnClick(::onShowRecordsCalendarClicked)
                checkboxSettingsReverseOrderInCalendar.setOnClick(::onReverseOrderInCalendarClicked)
                checkboxSettingsShowActivityFilters.setOnClick(::onShowActivityFiltersClicked)
                checkboxSettingsShowGoalsSeparately.setOnClick(::onShowGoalsSeparatelyClicked)
                checkboxSettingsUseMilitaryTime.setOnClick(::onUseMilitaryTimeClicked)
                checkboxSettingsUseMonthDayTime.setOnClick(::onUseMonthDayTimeClicked)
                checkboxSettingsUseProportionalMinutes.setOnClick(::onUseProportionalMinutesClicked)
                checkboxSettingsShowSeconds.setOnClick(::onShowSecondsClicked)
                checkboxSettingsKeepScreenOn.setOnClick(::onKeepScreenOnClicked)
                tvSettingsChangeCardSize.setOnClick(::onChangeCardSizeClick)
            }
        }

        with(layoutSettingsAdditional) {
            with(viewModel.additionalDelegate) {
                layoutSettingsAdditionalTitle.setOnClick(viewModel::onSettingsAdditionalClick)
                spinnerSettingsFirstDayOfWeek.onPositionSelected = ::onFirstDayOfWeekSelected
                spinnerSettingsRepeatButtonType.onPositionSelected = ::onRepeatButtonSelected
                groupSettingsStartOfDay.setOnClick(::onStartOfDayClicked)
                btnSettingsStartOfDaySign.setOnClick(::onStartOfDaySignClicked)
                checkboxSettingsKeepStatisticsRange.setOnClick(::onKeepStatisticsRangeClicked)
                groupSettingsIgnoreShortRecords.setOnClick(::onIgnoreShortRecordsClicked)
                checkboxSettingsShowRecordTagSelection.setOnClick(::onShowRecordTagSelectionClicked)
                checkboxSettingsRecordTagSelectionClose.setOnClick(::onRecordTagSelectionCloseClicked)
                checkboxSettingsRecordTagSelectionGeneral.setOnClick(::onRecordTagSelectionGeneralClicked)
                checkboxSettingsAutomatedTrackingSend.setOnClick(::onAutomatedTrackingSendEventsClicked)
                btnSettingsAutomatedTracking.setOnClick(::onAutomatedTrackingHelpClick)
            }
        }

        with(layoutSettingsRating) {
            with(viewModel.ratingDelegate) {
                layoutSettingsRate.setOnClick(::onRateClick)
                layoutSettingsFeedback.setOnClick(::onFeedbackClick)
            }
        }

        with(layoutSettingsBackup) {
            layoutSettingsBackupTitle.setOnClick(viewModel::onSettingsBackupClick)
            layoutSettingsSaveBackup.setOnClick(backupViewModel::onSaveClick)
            layoutSettingsRestoreBackup.setOnClick(backupViewModel::onRestoreClick)
            checkboxSettingsAutomaticBackup.setOnClick(backupViewModel::onAutomaticBackupClick)
        }

        with(layoutSettingsExportImport) {
            layoutSettingsExportImportTitle.setOnClick(viewModel::onSettingsExportImportClick)
            layoutSettingsExportCsv.setOnClick(backupViewModel::onExportCsvClick)
            layoutSettingsImportCsv.setOnClick(backupViewModel::onImportCsvClick)
            btnSettingsImportCsvHelp.setOnClick(backupViewModel::onImportCsvHelpClick)
            layoutSettingsExportIcs.setOnClick(backupViewModel::onExportIcsClick)
            checkboxSettingsAutomaticExport.setOnClick(backupViewModel::onAutomaticExportClick)
        }
    }

    override fun initViewModel(): Unit = with(binding) {
        with(viewModel) {
            content.observe(contentAdapter::replace)

            settingsNotificationsVisibility.observe { opened ->
                layoutSettingsNotifications.layoutSettingsNotificationsContent.visible = opened
                layoutSettingsNotifications.arrowSettingsNotifications
                    .apply { if (opened) rotateDown() else rotateUp() }
            }
            viewModel.settingsDisplayVisibility.observe { opened ->
                layoutSettingsDisplay.layoutSettingsDisplayContent.visible = opened
                layoutSettingsDisplay.arrowSettingsDisplay
                    .apply { if (opened) rotateDown() else rotateUp() }
            }
            viewModel.settingsAdditionalVisibility.observe { opened ->
                layoutSettingsAdditional.layoutSettingsAdditionalContent.visible = opened
                layoutSettingsAdditional.arrowSettingsAdditional
                    .apply { if (opened) rotateDown() else rotateUp() }
            }
            viewModel.settingsBackupVisibility.observe { opened ->
                layoutSettingsBackup.layoutSettingsBackupContent.visible = opened
                layoutSettingsBackup.arrowSettingsBackup
                    .apply { if (opened) rotateDown() else rotateUp() }
            }
            viewModel.settingsExportImportVisibility.observe { opened ->
                layoutSettingsExportImport.layoutSettingsExportImportContent.visible = opened
                layoutSettingsExportImport.arrowSettingsExportImport
                    .apply { if (opened) rotateDown() else rotateUp() }
            }
            resetScreen.observe {
                containerSettings.smoothScrollTo(0, 0)
                mainTabsViewModel.onHandled()
            }
        }

        with(viewModel.mainDelegate) {
            themeChanged.observe(::changeTheme)
        }

        with(viewModel.notificationsDelegate) {
            with(layoutSettingsNotifications) {
                showNotificationsCheckbox.observe(::updateShowNotifications)
                showNotificationsControlsCheckbox.observe(checkboxSettingsShowNotificationsControls::setChecked)
                inactivityReminderViewData.observe(::updateInactivityReminder)
                inactivityReminderRecurrentCheckbox.observe(checkboxSettingsInactivityReminderRecurrent::setChecked)
                inactivityReminderDndStartViewData.observe(tvSettingsInactivityReminderDndStart::setText)
                inactivityReminderDndEndViewData.observe(tvSettingsInactivityReminderDndEnd::setText)
                activityReminderViewData.observe(::updateActivityReminder)
                activityReminderRecurrentCheckbox.observe(checkboxSettingsActivityReminderRecurrent::setChecked)
                activityReminderDndStartViewData.observe(tvSettingsActivityReminderDndStart::setText)
                activityReminderDndEndViewData.observe(tvSettingsActivityReminderDndEnd::setText)
            }
        }

        with(viewModel.displayDelegate) {
            with(layoutSettingsDisplay) {
                btnCardOrderManualVisibility.observe(btnCardOrderManual::visible::set)
                showUntrackedInRecordsCheckbox.observe(checkboxSettingsShowUntrackedInRecords::setChecked)
                showUntrackedInStatisticsCheckbox.observe(checkboxSettingsShowUntrackedInStatistics::setChecked)
                ignoreShortUntrackedViewData.observe(tvSettingsIgnoreShortUntrackedTime::setText)
                untrackedRangeViewData.observe(::setUntrackedRangeViewData)
                showRecordsCalendarCheckbox.observe(::updateShowRecordCalendarChecked)
                daysInCalendarViewData.observe(::updateDaysInCalendarViewData)
                widgetTransparencyViewData.observe(::updateWidgetTransparencyViewData)
                reverseOrderInCalendarCheckbox.observe(checkboxSettingsReverseOrderInCalendar::setChecked)
                showActivityFiltersCheckbox.observe(checkboxSettingsShowActivityFilters::setChecked)
                showGoalsSeparatelyCheckbox.observe(checkboxSettingsShowGoalsSeparately::setChecked)
                useMilitaryTimeCheckbox.observe(checkboxSettingsUseMilitaryTime::setChecked)
                useMonthDayTimeCheckbox.observe(checkboxSettingsUseMonthDayTime::setChecked)
                useProportionalMinutesCheckbox.observe(checkboxSettingsUseProportionalMinutes::setChecked)
                showSecondsCheckbox.observe(checkboxSettingsShowSeconds::setChecked)
                useMilitaryTimeHint.observe(tvSettingsUseMilitaryTimeHint::setText)
                useMonthDayTimeHint.observe(tvSettingsUseMonthDayTimeHint::setText)
                useProportionalMinutesHint.observe(tvSettingsUseProportionalMinutesHint::setText)
                cardOrderViewData.observe(::updateCardOrderViewData)
                keepScreenOnCheckbox.observe(::setKeepScreenOn)
            }
        }

        with(viewModel.additionalDelegate) {
            with(layoutSettingsAdditional) {
                keepStatisticsRangeCheckbox.observe(checkboxSettingsKeepStatisticsRange::setChecked)
                ignoreShortRecordsViewData.observe(tvSettingsIgnoreShortRecordsTime::setText)
                recordTagSelectionCloseCheckbox.observe(checkboxSettingsRecordTagSelectionClose::setChecked)
                recordTagSelectionForGeneralTagsCheckbox.observe(checkboxSettingsRecordTagSelectionGeneral::setChecked)
                automatedTrackingSendEventsCheckbox.observe(checkboxSettingsAutomatedTrackingSend::setChecked)
                firstDayOfWeekViewData.observe(::updateFirstDayOfWeekViewData)
                repeatButtonViewData.observe(::updateRepeatButtonViewData)
                startOfDayViewData.observe(::updateStartOfDayViewData)
                showRecordTagSelectionCheckbox.observe(::updateShowRecordTagSelectionChecked)
            }
        }

        with(viewModel.ratingDelegate) {
            with(layoutSettingsRating) {
                versionName.observe(tvSettingsVersionName::setText)
            }
        }

        with(viewModel.translatorsDelegate) {
            translatorsViewData.observe(translatorsAdapter::replaceAsNew)
        }

        with(backupViewModel) {
            with(layoutSettingsBackup) {
                automaticBackupCheckbox.observe(checkboxSettingsAutomaticBackup::setChecked)
                automaticBackupLastSaveTime.observe {
                    tvSettingsAutomaticBackupLastSaveTime.visible = it.isNotEmpty()
                    tvSettingsAutomaticBackupLastSaveTime.text = it
                }
            }
            with(layoutSettingsExportImport) {
                automaticExportCheckbox.observe(checkboxSettingsAutomaticExport::setChecked)
                automaticExportLastSaveTime.observe {
                    tvSettingsAutomaticExportLastSaveTime.visible = it.isNotEmpty()
                    tvSettingsAutomaticExportLastSaveTime.text = it
                }
            }
        }

        with(mainTabsViewModel) {
            tabReselected.observe(viewModel::onTabReselected)
        }
    }

    override fun onResume() = with(binding) {
        super.onResume()
        with(layoutSettingsNotifications) {
            checkboxSettingsShowNotifications.jumpDrawablesToCurrentState()
            checkboxSettingsShowNotificationsControls.jumpDrawablesToCurrentState()
            checkboxSettingsInactivityReminderRecurrent.jumpDrawablesToCurrentState()
            checkboxSettingsActivityReminderRecurrent.jumpDrawablesToCurrentState()
        }
        with(layoutSettingsDisplay) {
            spinnerSettingsDaysInCalendar.jumpDrawablesToCurrentState()
            spinnerSettingsWidgetTransparency.jumpDrawablesToCurrentState()
            spinnerSettingsRecordTypeSort.jumpDrawablesToCurrentState()
            checkboxSettingsShowUntrackedInRecords.jumpDrawablesToCurrentState()
            checkboxSettingsShowUntrackedInStatistics.jumpDrawablesToCurrentState()
            checkboxSettingsUntrackedRange.jumpDrawablesToCurrentState()
            checkboxSettingsShowRecordsCalendar.jumpDrawablesToCurrentState()
            checkboxSettingsReverseOrderInCalendar.jumpDrawablesToCurrentState()
            checkboxSettingsShowActivityFilters.jumpDrawablesToCurrentState()
            checkboxSettingsShowGoalsSeparately.jumpDrawablesToCurrentState()
            checkboxSettingsUseMilitaryTime.jumpDrawablesToCurrentState()
            checkboxSettingsUseMonthDayTime.jumpDrawablesToCurrentState()
            checkboxSettingsUseProportionalMinutes.jumpDrawablesToCurrentState()
            checkboxSettingsShowSeconds.jumpDrawablesToCurrentState()
            checkboxSettingsKeepScreenOn.jumpDrawablesToCurrentState()
        }
        with(layoutSettingsAdditional) {
            spinnerSettingsFirstDayOfWeek.jumpDrawablesToCurrentState()
            spinnerSettingsRepeatButtonType.jumpDrawablesToCurrentState()
            checkboxSettingsKeepStatisticsRange.jumpDrawablesToCurrentState()
            checkboxSettingsShowRecordTagSelection.jumpDrawablesToCurrentState()
            checkboxSettingsRecordTagSelectionClose.jumpDrawablesToCurrentState()
            checkboxSettingsRecordTagSelectionGeneral.jumpDrawablesToCurrentState()
            checkboxSettingsAutomatedTrackingSend.jumpDrawablesToCurrentState()
        }
        with(layoutSettingsBackup) {
            checkboxSettingsAutomaticBackup.jumpDrawablesToCurrentState()
        }
        with(layoutSettingsExportImport) {
            checkboxSettingsAutomaticExport.jumpDrawablesToCurrentState()
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

    private fun updateDaysInCalendarViewData(
        viewData: DaysInCalendarViewData,
    ) = with(binding.layoutSettingsDisplay) {
        spinnerSettingsDaysInCalendar.setData(viewData.items, viewData.selectedPosition)
        tvSettingsDaysInCalendarValue.text = viewData.items
            .getOrNull(viewData.selectedPosition)?.text.orEmpty()
    }

    private fun updateWidgetTransparencyViewData(
        viewData: WidgetTransparencyViewData,
    ) = with(binding.layoutSettingsDisplay) {
        spinnerSettingsWidgetTransparency.setData(viewData.items, viewData.selectedPosition)
        tvSettingsWidgetTransparencyValue.text = viewData.selectedValue
    }

    private fun updateFirstDayOfWeekViewData(
        viewData: FirstDayOfWeekViewData,
    ) = with(binding.layoutSettingsAdditional) {
        spinnerSettingsFirstDayOfWeek.setData(viewData.items, viewData.selectedPosition)
        tvSettingsFirstDayOfWeekValue.text = viewData.items
            .getOrNull(viewData.selectedPosition)?.text.orEmpty()
    }

    private fun updateRepeatButtonViewData(
        viewData: RepeatButtonViewData,
    ) = with(binding.layoutSettingsAdditional) {
        spinnerSettingsRepeatButtonType.setData(viewData.items, viewData.selectedPosition)
        tvSettingsRepeatButtonTypeValue.text = viewData.items
            .getOrNull(viewData.selectedPosition)?.text.orEmpty()
    }

    private fun updateShowRecordTagSelectionChecked(
        isChecked: Boolean,
    ) = with(binding.layoutSettingsAdditional) {
        checkboxSettingsShowRecordTagSelection.isChecked = isChecked
        groupSettingsRecordTagSelectionClose.visible = isChecked
    }

    private fun updateShowNotifications(
        isChecked: Boolean,
    ) = with(binding.layoutSettingsNotifications) {
        checkboxSettingsShowNotifications.isChecked = isChecked
        groupSettingsShowNotifications.visible = isChecked
    }

    private fun updateInactivityReminder(
        data: SettingsDurationViewData,
    ) = with(binding.layoutSettingsNotifications) {
        tvSettingsInactivityReminderTime.text = data.text
        groupSettingsInactivityReminderRecurrent.isVisible = data.enabled
    }

    private fun updateActivityReminder(
        data: SettingsDurationViewData,
    ) = with(binding.layoutSettingsNotifications) {
        tvSettingsActivityReminderTime.text = data.text
        groupSettingsActivityReminderRecurrent.isVisible = data.enabled
    }

    private fun updateShowRecordCalendarChecked(
        isChecked: Boolean,
    ) = with(binding.layoutSettingsDisplay) {
        checkboxSettingsShowRecordsCalendar.isChecked = isChecked
        groupSettingsReverseOrderInCalendar.visible = isChecked
    }

    private fun setUntrackedRangeViewData(
        viewData: SettingsUntrackedRangeViewData,
    ) = with(binding.layoutSettingsDisplay) {
        groupSettingsUntrackedRange.visible = viewData is SettingsUntrackedRangeViewData.Enabled
        checkboxSettingsUntrackedRange.isChecked = viewData is SettingsUntrackedRangeViewData.Enabled

        if (viewData is SettingsUntrackedRangeViewData.Enabled) {
            tvSettingsUntrackedRangeStart.text = viewData.rangeStart
            tvSettingsUntrackedRangeEnd.text = viewData.rangeEnd
        }
    }

    private fun updateStartOfDayViewData(
        viewData: SettingsStartOfDayViewData,
    ) = with(binding.layoutSettingsAdditional) {
        tvSettingsStartOfDayTime.text = viewData.startOfDayValue
        btnSettingsStartOfDaySign.visible = viewData.startOfDaySign.isNotEmpty()
        tvSettingsStartOfDaySign.text = viewData.startOfDaySign
        tvSettingsStartOfDayHintValue.text = viewData.hint
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
            activity?.recreate()
            // TODO fix fade and save scroll
            activity?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    companion object {
        fun newInstance(): SettingsFragment = SettingsFragment()
    }
}
