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
import com.example.util.simpletimetracker.feature_settings.adapter.createSettingsTranslatorAdapterDelegate
import com.example.util.simpletimetracker.feature_settings.viewData.CardOrderViewData
import com.example.util.simpletimetracker.feature_settings.viewData.DarkModeViewData
import com.example.util.simpletimetracker.feature_settings.viewData.DaysInCalendarViewData
import com.example.util.simpletimetracker.feature_settings.viewData.FirstDayOfWeekViewData
import com.example.util.simpletimetracker.feature_settings.viewData.LanguageViewData
import com.example.util.simpletimetracker.feature_settings.viewData.SettingsDurationViewData
import com.example.util.simpletimetracker.feature_settings.viewData.SettingsStartOfDayViewData
import com.example.util.simpletimetracker.feature_settings.viewData.SettingsUntrackedRangeViewData
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
    private val backupViewModel: BackupViewModel by activityViewModels(
        factoryProducer = { backupViewModelFactory }
    )
    private val mainTabsViewModel: MainTabsViewModel by activityViewModels(
        factoryProducer = { mainTabsViewModelFactory }
    )

    private val translatorsAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createSettingsTranslatorAdapterDelegate()
        )
    }

    override fun initUi() = with(binding) {
        layoutSettingsTranslators.rvSettingsTranslators.adapter = translatorsAdapter
        with(layoutSettingsMain) {
            spinnerSettingsDarkMode.setProcessSameItemSelection(false)
        }
        with(layoutSettingsDisplay) {
            spinnerSettingsDaysInCalendar.setProcessSameItemSelection(false)
            spinnerSettingsRecordTypeSort.setProcessSameItemSelection(false)
        }
        with(layoutSettingsAdditional) {
            spinnerSettingsFirstDayOfWeek.setProcessSameItemSelection(false)
        }
    }

    override fun initUx() = with(binding) {
        with(layoutSettingsMain) {
            checkboxSettingsAllowMultitasking.setOnClick(viewModel::onAllowMultitaskingClicked)
            spinnerSettingsDarkMode.onPositionSelected = viewModel::onDarkModeSelected
            spinnerSettingsLanguage.onPositionSelected = viewModel::onLanguageSelected
            layoutSettingsEditCategories.setOnClick(throttle(viewModel::onEditCategoriesClick))
            tvSettingsArchive.setOnClick(throttle(viewModel::onArchiveClick))
            tvSettingsDataEdit.setOnClick(throttle(viewModel::onDataEditClick))
        }
        with(layoutSettingsNotifications) {
            layoutSettingsNotificationsTitle.setOnClick(viewModel::onSettingsNotificationsClick)
            checkboxSettingsShowNotifications.setOnClick(viewModel::onShowNotificationsClicked)
            checkboxSettingsShowNotificationsControls.setOnClick(viewModel::onShowNotificationsControlsClicked)
            groupSettingsInactivityReminder.setOnClick(viewModel::onInactivityReminderClicked)
            checkboxSettingsInactivityReminderRecurrent.setOnClick(viewModel::onInactivityReminderRecurrentClicked)
            tvSettingsInactivityReminderDndStart.setOnClick(viewModel::onInactivityReminderDoNotDisturbStartClicked)
            tvSettingsInactivityReminderDndEnd.setOnClick(viewModel::onInactivityReminderDoNotDisturbEndClicked)
            groupSettingsActivityReminder.setOnClick(viewModel::onActivityReminderClicked)
            checkboxSettingsActivityReminderRecurrent.setOnClick(viewModel::onActivityReminderRecurrentClicked)
            tvSettingsActivityReminderDndStart.setOnClick(viewModel::onActivityReminderDoNotDisturbStartClicked)
            tvSettingsActivityReminderDndEnd.setOnClick(viewModel::onActivityReminderDoNotDisturbEndClicked)
        }
        with(layoutSettingsDisplay) {
            layoutSettingsDisplayTitle.setOnClick(viewModel::onSettingsDisplayClick)
            spinnerSettingsDaysInCalendar.onPositionSelected = viewModel::onDaysInCalendarSelected
            spinnerSettingsRecordTypeSort.onPositionSelected = viewModel::onRecordTypeOrderSelected
            btnCardOrderManual.setOnClick(viewModel::onCardOrderManualClick)
            checkboxSettingsShowUntrackedInRecords.setOnClick(viewModel::onShowUntrackedInRecordsClicked)
            checkboxSettingsShowUntrackedInStatistics.setOnClick(viewModel::onShowUntrackedInStatisticsClicked)
            groupSettingsIgnoreShortUntracked.setOnClick(viewModel::onIgnoreShortUntrackedClicked)
            checkboxSettingsUntrackedRange.setOnClick(viewModel::onUntrackedRangeClicked)
            tvSettingsUntrackedRangeStart.setOnClick(viewModel::onUntrackedRangeStartClicked)
            tvSettingsUntrackedRangeEnd.setOnClick(viewModel::onUntrackedRangeEndClicked)
            checkboxSettingsShowRecordsCalendar.setOnClick(viewModel::onShowRecordsCalendarClicked)
            checkboxSettingsReverseOrderInCalendar.setOnClick(viewModel::onReverseOrderInCalendarClicked)
            checkboxSettingsShowActivityFilters.setOnClick(viewModel::onShowActivityFiltersClicked)
            checkboxSettingsShowGoalsSeparately.setOnClick(viewModel::onShowGoalsSeparatelyClicked)
            checkboxSettingsUseMilitaryTime.setOnClick(viewModel::onUseMilitaryTimeClicked)
            checkboxSettingsUseMonthDayTime.setOnClick(viewModel::onUseMonthDayTimeClicked)
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
            groupSettingsIgnoreShortRecords.setOnClick(viewModel::onIgnoreShortRecordsClicked)
            checkboxSettingsShowRecordTagSelection.setOnClick(viewModel::onShowRecordTagSelectionClicked)
            checkboxSettingsRecordTagSelectionClose.setOnClick(viewModel::onRecordTagSelectionCloseClicked)
            checkboxSettingsRecordTagSelectionGeneral.setOnClick(viewModel::onRecordTagSelectionGeneralClicked)
            checkboxSettingsAutomatedTrackingSend.setOnClick(viewModel::onAutomatedTrackingSendEventsClicked)
            btnSettingsAutomatedTracking.setOnClick(viewModel::onAutomatedTrackingHelpClick)
        }
        with(layoutSettingsRating) {
            layoutSettingsRate.setOnClick(viewModel::onRateClick)
            layoutSettingsFeedback.setOnClick(viewModel::onFeedbackClick)
        }
        with(layoutSettingsBackup) {
            layoutSettingsBackupTitle.setOnClick(viewModel::onSettingsBackupClick)
            layoutSettingsSaveBackup.setOnClick(backupViewModel::onSaveClick)
            layoutSettingsRestoreBackup.setOnClick(backupViewModel::onRestoreClick)
            layoutSettingsExportCsv.setOnClick(backupViewModel::onExportCsvClick)
            layoutSettingsImportCsv.setOnClick(backupViewModel::onImportCsvClick)
            btnSettingsImportCsvHelp.setOnClick(backupViewModel::onImportCsvHelpClick)
            layoutSettingsExportIcs.setOnClick(backupViewModel::onExportIcsClick)
            checkboxSettingsAutomaticBackup.setOnClick(backupViewModel::onAutomaticBackupClick)
            checkboxSettingsAutomaticExport.setOnClick(backupViewModel::onAutomaticExportClick)
        }
    }

    override fun initViewModel(): Unit = with(binding) {
        with(viewModel) {
            with(layoutSettingsMain) {
                allowMultitaskingCheckbox.observe(checkboxSettingsAllowMultitasking::setChecked)
                darkModeViewData.observe(::updateDarkModeViewData)
                languageViewData.observe(::updateLanguageViewData)
            }
            with(layoutSettingsNotifications) {
                settingsNotificationsVisibility.observe { opened ->
                    layoutSettingsNotificationsContent.visible = opened
                    arrowSettingsNotifications.apply { if (opened) rotateDown() else rotateUp() }
                }
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
            with(layoutSettingsDisplay) {
                settingsDisplayVisibility.observe { opened ->
                    layoutSettingsDisplayContent.visible = opened
                    arrowSettingsDisplay.apply { if (opened) rotateDown() else rotateUp() }
                }
                btnCardOrderManualVisibility.observe(btnCardOrderManual::visible::set)
                showUntrackedInRecordsCheckbox.observe(checkboxSettingsShowUntrackedInRecords::setChecked)
                showUntrackedInStatisticsCheckbox.observe(checkboxSettingsShowUntrackedInStatistics::setChecked)
                ignoreShortUntrackedViewData.observe(tvSettingsIgnoreShortUntrackedTime::setText)
                untrackedRangeViewData.observe(::setUntrackedRangeViewData)
                showRecordsCalendarCheckbox.observe(::updateShowRecordCalendarChecked)
                daysInCalendarViewData.observe(::updateDaysInCalendarViewData)
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
            with(layoutSettingsAdditional) {
                settingsAdditionalVisibility.observe { opened ->
                    layoutSettingsAdditionalContent.visible = opened
                    arrowSettingsAdditional.apply { if (opened) rotateDown() else rotateUp() }
                }
                keepStatisticsRangeCheckbox.observe(checkboxSettingsKeepStatisticsRange::setChecked)
                ignoreShortRecordsViewData.observe(tvSettingsIgnoreShortRecordsTime::setText)
                recordTagSelectionCloseCheckbox.observe(checkboxSettingsRecordTagSelectionClose::setChecked)
                recordTagSelectionForGeneralTagsCheckbox.observe(checkboxSettingsRecordTagSelectionGeneral::setChecked)
                automatedTrackingSendEventsCheckbox.observe(checkboxSettingsAutomatedTrackingSend::setChecked)
                firstDayOfWeekViewData.observe(::updateFirstDayOfWeekViewData)
                startOfDayViewData.observe(::updateStartOfDayViewData)
                showRecordTagSelectionCheckbox.observe(::updateShowRecordTagSelectionChecked)
            }
            with(layoutSettingsRating) {
                versionName.observe(tvSettingsVersionName::setText)
            }
            translatorsViewData.observe(translatorsAdapter::replaceAsNew)
            themeChanged.observe(::changeTheme)
            resetScreen.observe {
                containerSettings.smoothScrollTo(0, 0)
                mainTabsViewModel.onHandled()
            }
        }
        with(backupViewModel) {
            with(layoutSettingsBackup) {
                viewModel.settingsBackupVisibility.observe { opened ->
                    layoutSettingsBackupContent.visible = opened
                    arrowSettingsBackup.apply { if (opened) rotateDown() else rotateUp() }
                }
                automaticBackupCheckbox.observe(checkboxSettingsAutomaticBackup::setChecked)
                automaticBackupLastSaveTime.observe {
                    tvSettingsAutomaticBackupLastSaveTime.visible = it.isNotEmpty()
                    tvSettingsAutomaticBackupLastSaveTime.text = it
                }
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
        with(layoutSettingsMain) {
            checkboxSettingsAllowMultitasking.jumpDrawablesToCurrentState()
            spinnerSettingsDarkMode.jumpDrawablesToCurrentState()
            spinnerSettingsLanguage.jumpDrawablesToCurrentState()
        }
        with(layoutSettingsNotifications) {
            checkboxSettingsShowNotifications.jumpDrawablesToCurrentState()
            checkboxSettingsShowNotificationsControls.jumpDrawablesToCurrentState()
            checkboxSettingsInactivityReminderRecurrent.jumpDrawablesToCurrentState()
            checkboxSettingsActivityReminderRecurrent.jumpDrawablesToCurrentState()
        }
        with(layoutSettingsDisplay) {
            spinnerSettingsDaysInCalendar.jumpDrawablesToCurrentState()
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
            checkboxSettingsKeepStatisticsRange.jumpDrawablesToCurrentState()
            checkboxSettingsShowRecordTagSelection.jumpDrawablesToCurrentState()
            checkboxSettingsRecordTagSelectionClose.jumpDrawablesToCurrentState()
            checkboxSettingsRecordTagSelectionGeneral.jumpDrawablesToCurrentState()
            checkboxSettingsAutomatedTrackingSend.jumpDrawablesToCurrentState()
        }
        with(layoutSettingsBackup) {
            checkboxSettingsAutomaticBackup.jumpDrawablesToCurrentState()
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

    private fun updateFirstDayOfWeekViewData(
        viewData: FirstDayOfWeekViewData,
    ) = with(binding.layoutSettingsAdditional) {
        spinnerSettingsFirstDayOfWeek.setData(viewData.items, viewData.selectedPosition)
        tvSettingsFirstDayOfWeekValue.text = viewData.items
            .getOrNull(viewData.selectedPosition)?.text.orEmpty()
    }

    private fun updateDarkModeViewData(
        viewData: DarkModeViewData,
    ) = with(binding.layoutSettingsMain) {
        spinnerSettingsDarkMode.setData(viewData.items, viewData.selectedPosition)
        tvSettingsDarkModeValue.text = viewData.items
            .getOrNull(viewData.selectedPosition)?.text.orEmpty()
    }

    private fun updateLanguageViewData(
        viewData: LanguageViewData,
    ) = with(binding.layoutSettingsMain) {
        spinnerSettingsLanguage.setData(viewData.items, -1)
        tvSettingsLanguageValue.text = viewData.currentLanguageName
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
