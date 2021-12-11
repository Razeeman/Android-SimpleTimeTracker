package com.example.util.simpletimetracker.feature_settings.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.BuildConfig
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.dialog.CsvExportSettingsDialogListener
import com.example.util.simpletimetracker.core.dialog.DateTimeDialogListener
import com.example.util.simpletimetracker.core.dialog.DurationDialogListener
import com.example.util.simpletimetracker.core.dialog.StandardDialogListener
import com.example.util.simpletimetracker.core.sharedViewModel.BackupViewModel
import com.example.util.simpletimetracker.feature_settings.viewData.CardOrderViewData
import com.example.util.simpletimetracker.feature_settings.viewData.FirstDayOfWeekViewData
import com.example.util.simpletimetracker.feature_settings.viewModel.SettingsViewModel
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.navigation.params.screen.CsvExportSettingsParams
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.example.util.simpletimetracker.feature_settings.databinding.SettingsFragmentBinding as Binding

@AndroidEntryPoint
class SettingsFragment :
    BaseFragment<Binding>(),
    StandardDialogListener,
    DurationDialogListener,
    DateTimeDialogListener,
    CsvExportSettingsDialogListener {

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
        tvSettingsVersionName.text = BuildConfig.VERSION_NAME
    }

    override fun initUx() = with(binding) {
        spinnerSettingsRecordTypeSort.onPositionSelected = viewModel::onRecordTypeOrderSelected
        btnCardOrderManual.setOnClick(viewModel::onCardOrderManualClick)
        spinnerSettingsFirstDayOfWeek.onPositionSelected = viewModel::onFirstDayOfWeekSelected
        groupSettingsStartOfDay.setOnClick(viewModel::onStartOfDayClicked)
        checkboxSettingsShowUntracked.setOnClick(viewModel::onShowUntrackedClicked)
        checkboxSettingsAllowMultitasking.setOnClick(viewModel::onAllowMultitaskingClicked)
        checkboxSettingsShowNotifications.setOnClick(viewModel::onShowNotificationsClicked)
        groupSettingsInactivityReminder.setOnClick(viewModel::onInactivityReminderClicked)
        checkboxSettingsDarkMode.setOnClick(viewModel::onDarkModeClicked)
        checkboxSettingsUseMilitaryTime.setOnClick(viewModel::onUseMilitaryTimeClicked)
        checkboxSettingsUseProportionalMinutes.setOnClick(viewModel::onUseProportionalMinutesClicked)
        tvSettingsChangeCardSize.setOnClick(viewModel::onChangeCardSizeClick)
        layoutSettingsEditCategories.setOnClick(viewModel::onEditCategoriesClick)
        checkboxSettingsShowRecordTagSelection.setOnClick(viewModel::onShowRecordTagSelectionClicked)
        checkboxSettingsRecordTagSelectionClose.setOnClick(viewModel::onRecordTagSelectionCloseClicked)
        tvSettingsArchive.setOnClick(viewModel::onArchiveClick)
        layoutSettingsSaveBackup.setOnClick(backupViewModel::onSaveClick)
        layoutSettingsRestoreBackup.setOnClick(backupViewModel::onRestoreClick)
        layoutSettingsExportCsv.setOnClick(viewModel::onExportCsvClick)
        layoutSettingsRate.setOnClick(viewModel::onRateClick)
        layoutSettingsFeedback.setOnClick(viewModel::onFeedbackClick)
    }

    override fun initViewModel(): Unit = with(binding) {
        with(viewModel) {
            cardOrderViewData.observe(::updateCardOrderViewData)
            firstDayOfWeekViewData.observe(::updateFirstDayOfWeekViewData)
            startOfDayViewData.observe(tvSettingsStartOfDayTime::setText)
            btnCardOrderManualVisibility.observe(btnCardOrderManual::visible::set)
            showUntrackedCheckbox.observe(checkboxSettingsShowUntracked::setChecked)
            allowMultitaskingCheckbox.observe(checkboxSettingsAllowMultitasking::setChecked)
            showNotificationsCheckbox.observe(checkboxSettingsShowNotifications::setChecked)
            inactivityReminderViewData.observe(tvSettingsInactivityReminderTime::setText)
            darkModeCheckbox.observe(checkboxSettingsDarkMode::setChecked)
            useMilitaryTimeCheckbox.observe(checkboxSettingsUseMilitaryTime::setChecked)
            useProportionalMinutesCheckbox.observe(checkboxSettingsUseProportionalMinutes::setChecked)
            showRecordTagSelectionCheckbox.observe(::updateShowRecordTagSelectionChecked)
            recordTagSelectionCloseCheckbox.observe(checkboxSettingsRecordTagSelectionClose::setChecked)
            useMilitaryTimeHint.observe(tvSettingsUseMilitaryTimeHint::setText)
            useProportionalMinutesHint.observe(tvSettingsUseProportionalMinutesHint::setText)
            themeChanged.observe(::changeTheme)
        }
    }

    override fun onResume() {
        super.onResume()
        with(binding) {
            spinnerSettingsRecordTypeSort.jumpDrawablesToCurrentState()
            spinnerSettingsFirstDayOfWeek.jumpDrawablesToCurrentState()
            checkboxSettingsShowUntracked.jumpDrawablesToCurrentState()
            checkboxSettingsAllowMultitasking.jumpDrawablesToCurrentState()
            checkboxSettingsShowNotifications.jumpDrawablesToCurrentState()
            checkboxSettingsDarkMode.jumpDrawablesToCurrentState()
            checkboxSettingsUseMilitaryTime.jumpDrawablesToCurrentState()
            checkboxSettingsShowRecordTagSelection.jumpDrawablesToCurrentState()
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

    override fun onCsvExportSettingsSelected(data: CsvExportSettingsParams) {
        backupViewModel.onCsvExportSettingsSelected(data)
    }

    private fun updateCardOrderViewData(viewData: CardOrderViewData) = with(binding) {
        btnCardOrderManual.visible = viewData.isManualConfigButtonVisible
        spinnerSettingsRecordTypeSort.setData(viewData.items, viewData.selectedPosition)
        tvSettingsRecordTypeSortValue.text = viewData.items
            .getOrNull(viewData.selectedPosition)?.text.orEmpty()
    }

    private fun updateFirstDayOfWeekViewData(viewData: FirstDayOfWeekViewData) = with(binding) {
        spinnerSettingsFirstDayOfWeek.setData(viewData.items, viewData.selectedPosition)
        tvSettingsFirstDayOfWeekValue.text = viewData.items
            .getOrNull(viewData.selectedPosition)?.text.orEmpty()
    }

    private fun updateShowRecordTagSelectionChecked(isChecked: Boolean) = with(binding) {
        checkboxSettingsShowRecordTagSelection.isChecked = isChecked
        groupSettingsRecordTagSelectionClose.visible = isChecked
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
