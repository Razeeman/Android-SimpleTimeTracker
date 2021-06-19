package com.example.util.simpletimetracker.feature_settings.view

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.dialog.DurationDialogListener
import com.example.util.simpletimetracker.core.dialog.StandardDialogListener
import com.example.util.simpletimetracker.core.extension.setOnClick
import com.example.util.simpletimetracker.core.extension.visible
import com.example.util.simpletimetracker.core.viewModel.BackupViewModel
import com.example.util.simpletimetracker.feature_settings.R
import com.example.util.simpletimetracker.feature_settings.viewData.CardOrderViewData
import com.example.util.simpletimetracker.feature_settings.viewData.FirstDayOfWeekViewData
import com.example.util.simpletimetracker.feature_settings.viewModel.SettingsViewModel
import com.example.util.simpletimetracker.navigation.RequestCode.REQUEST_CODE_CREATE_CSV_FILE
import com.example.util.simpletimetracker.navigation.RequestCode.REQUEST_CODE_CREATE_FILE
import com.example.util.simpletimetracker.navigation.RequestCode.REQUEST_CODE_OPEN_FILE
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.settings_fragment.btnCardOrderManual
import kotlinx.android.synthetic.main.settings_fragment.checkboxSettingsAllowMultitasking
import kotlinx.android.synthetic.main.settings_fragment.checkboxSettingsDarkMode
import kotlinx.android.synthetic.main.settings_fragment.checkboxSettingsShowNotifications
import kotlinx.android.synthetic.main.settings_fragment.checkboxSettingsShowRecordTagSelection
import kotlinx.android.synthetic.main.settings_fragment.checkboxSettingsShowUntracked
import kotlinx.android.synthetic.main.settings_fragment.checkboxSettingsUseMilitaryTime
import kotlinx.android.synthetic.main.settings_fragment.groupSettingsInactivityReminder
import kotlinx.android.synthetic.main.settings_fragment.layoutSettingsEditCategories
import kotlinx.android.synthetic.main.settings_fragment.layoutSettingsExportCsv
import kotlinx.android.synthetic.main.settings_fragment.layoutSettingsFeedback
import kotlinx.android.synthetic.main.settings_fragment.layoutSettingsRate
import kotlinx.android.synthetic.main.settings_fragment.layoutSettingsRestoreBackup
import kotlinx.android.synthetic.main.settings_fragment.layoutSettingsSaveBackup
import kotlinx.android.synthetic.main.settings_fragment.spinnerSettingsFirstDayOfWeek
import kotlinx.android.synthetic.main.settings_fragment.spinnerSettingsRecordTypeSort
import kotlinx.android.synthetic.main.settings_fragment.tvSettingsArchive
import kotlinx.android.synthetic.main.settings_fragment.tvSettingsChangeCardSize
import kotlinx.android.synthetic.main.settings_fragment.tvSettingsFirstDayOfWeekValue
import kotlinx.android.synthetic.main.settings_fragment.tvSettingsInactivityReminderTime
import kotlinx.android.synthetic.main.settings_fragment.tvSettingsRecordTypeSortValue
import kotlinx.android.synthetic.main.settings_fragment.tvSettingsUseMilitaryTimeHint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : BaseFragment(),
    StandardDialogListener,
    DurationDialogListener {

    override val layout: Int get() = R.layout.settings_fragment

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

    override fun initUx() {
        spinnerSettingsRecordTypeSort.onPositionSelected = viewModel::onRecordTypeOrderSelected
        btnCardOrderManual.setOnClick(viewModel::onCardOrderManualClick)
        spinnerSettingsFirstDayOfWeek.onPositionSelected = viewModel::onFirstDayOfWeekSelected
        checkboxSettingsShowUntracked.setOnClick(viewModel::onShowUntrackedClicked)
        checkboxSettingsAllowMultitasking.setOnClick(viewModel::onAllowMultitaskingClicked)
        checkboxSettingsShowNotifications.setOnClick(viewModel::onShowNotificationsClicked)
        groupSettingsInactivityReminder.setOnClick(viewModel::onInactivityReminderClicked)
        checkboxSettingsDarkMode.setOnClick(viewModel::onDarkModeClicked)
        checkboxSettingsUseMilitaryTime.setOnClick(viewModel::onUseMilitaryTimeClicked)
        tvSettingsChangeCardSize.setOnClick(viewModel::onChangeCardSizeClick)
        layoutSettingsEditCategories.setOnClick(viewModel::onEditCategoriesClick)
        checkboxSettingsShowRecordTagSelection.setOnClick(viewModel::onShowRecordTagSelectionClicked)
        tvSettingsArchive.setOnClick(viewModel::onArchiveClick)
        layoutSettingsSaveBackup.setOnClick(viewModel::onSaveClick)
        layoutSettingsRestoreBackup.setOnClick(viewModel::onRestoreClick)
        layoutSettingsExportCsv.setOnClick(viewModel::onExportCsvClick)
        layoutSettingsRate.setOnClick(viewModel::onRateClick)
        layoutSettingsFeedback.setOnClick(viewModel::onFeedbackClick)
    }

    override fun initViewModel(): Unit = with(viewModel) {
        cardOrderViewData.observe(::updateCardOrderViewData)
        firstDayOfWeekViewData.observe(::updateFirstDayOfWeekViewData)
        btnCardOrderManualVisibility.observe(btnCardOrderManual::visible::set)
        showUntrackedCheckbox.observe(checkboxSettingsShowUntracked::setChecked)
        allowMultitaskingCheckbox.observe(checkboxSettingsAllowMultitasking::setChecked)
        showNotificationsCheckbox.observe(checkboxSettingsShowNotifications::setChecked)
        inactivityReminderViewData.observe(tvSettingsInactivityReminderTime::setText)
        darkModeCheckbox.observe(checkboxSettingsDarkMode::setChecked)
        useMilitaryTimeCheckbox.observe(checkboxSettingsUseMilitaryTime::setChecked)
        showRecordTagSelectionCheckbox.observe(checkboxSettingsShowRecordTagSelection::setChecked)
        useMilitaryTimeHint.observe(tvSettingsUseMilitaryTimeHint::setText)
        themeChanged.observe(::changeTheme)
    }

    override fun onResume() {
        super.onResume()
        spinnerSettingsRecordTypeSort.jumpDrawablesToCurrentState()
        spinnerSettingsFirstDayOfWeek.jumpDrawablesToCurrentState()
        checkboxSettingsShowUntracked.jumpDrawablesToCurrentState()
        checkboxSettingsAllowMultitasking.jumpDrawablesToCurrentState()
        checkboxSettingsShowNotifications.jumpDrawablesToCurrentState()
        checkboxSettingsDarkMode.jumpDrawablesToCurrentState()
        checkboxSettingsUseMilitaryTime.jumpDrawablesToCurrentState()
        checkboxSettingsShowRecordTagSelection.jumpDrawablesToCurrentState()
        viewModel.onVisible()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (data != null && resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_CREATE_FILE -> {
                    data.data
                        ?.let(Uri::toString)
                        ?.let(backupViewModel::onSaveBackup)
                }
                REQUEST_CODE_OPEN_FILE -> {
                    data.data
                        ?.let(Uri::toString)
                        ?.let(backupViewModel::onRestoreBackup)
                }
                REQUEST_CODE_CREATE_CSV_FILE -> {
                    data.data
                        ?.let(Uri::toString)
                        ?.let(backupViewModel::onSaveCsvFile)
                }
            }
        }
    }

    override fun onPositiveClick(tag: String?, data: Any?) {
        viewModel.onPositiveDialogClick(tag)
    }

    override fun onDurationSet(duration: Long, tag: String?) {
        viewModel.onDurationSet(tag, duration)
    }

    override fun onDisable(tag: String?) {
        viewModel.onDurationDisabled(tag)
    }

    private fun updateCardOrderViewData(viewData: CardOrderViewData) {
        btnCardOrderManual.visible = viewData.isManualConfigButtonVisible
        spinnerSettingsRecordTypeSort.setData(viewData.items, viewData.selectedPosition)
        tvSettingsRecordTypeSortValue.text = viewData.items.getOrNull(viewData.selectedPosition)?.text.orEmpty()
    }

    private fun updateFirstDayOfWeekViewData(viewData: FirstDayOfWeekViewData) {
        spinnerSettingsFirstDayOfWeek.setData(viewData.items, viewData.selectedPosition)
        tvSettingsFirstDayOfWeekValue.text = viewData.items.getOrNull(viewData.selectedPosition)?.text.orEmpty()
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
