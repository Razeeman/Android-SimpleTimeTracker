package com.example.util.simpletimetracker.feature_settings.view

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.dialog.StandardDialogListener
import com.example.util.simpletimetracker.core.extension.setOnClick
import com.example.util.simpletimetracker.core.viewModel.BackupViewModel
import com.example.util.simpletimetracker.feature_settings.BuildConfig
import com.example.util.simpletimetracker.feature_settings.R
import com.example.util.simpletimetracker.feature_settings.di.SettingsComponentProvider
import com.example.util.simpletimetracker.feature_settings.viewModel.SettingsViewModel
import com.example.util.simpletimetracker.navigation.RequestCode.REQUEST_CODE_CREATE_FILE
import com.example.util.simpletimetracker.navigation.RequestCode.REQUEST_CODE_OPEN_FILE
import kotlinx.android.synthetic.main.settings_fragment.*
import javax.inject.Inject

class SettingsFragment : BaseFragment(R.layout.settings_fragment),
    StandardDialogListener {

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

    override fun initDi() {
        (activity?.application as SettingsComponentProvider)
            .settingsComponent
            ?.inject(this)
        tvSettingsVersionName.text = BuildConfig.VERSION_NAME
    }

    override fun initUx() {
        checkboxSettingsRecordTypeSort.setOnClick(viewModel::onRecordTypeSortClicked)
        checkboxSettingsShowUntracked.setOnClick(viewModel::onShowUntrackedClicked)
        layoutSettingsSaveBackup.setOnClick(viewModel::onSaveClick)
        tvSettingsRestoreBackup.setOnClick(viewModel::onRestoreClick)
        layoutSettingsRate.setOnClick(viewModel::onRateClick)
        layoutSettingsFeedback.setOnClick(viewModel::onFeedbackClick)
    }

    override fun initViewModel(): Unit = with(viewModel) {
        sortRecordTypesCheckbox.observe(viewLifecycleOwner, checkboxSettingsRecordTypeSort::setChecked)
        showUntrackedCheckbox.observe(viewLifecycleOwner, checkboxSettingsShowUntracked::setChecked)
    }

    override fun onResume() {
        super.onResume()
        checkboxSettingsRecordTypeSort.jumpDrawablesToCurrentState()
        checkboxSettingsShowUntracked.jumpDrawablesToCurrentState()
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
            }
        }
    }

    override fun onPositiveClick(tag: String?) {
        viewModel.onPositiveDialogClick(tag)
    }

    companion object {
        fun newInstance(): SettingsFragment = SettingsFragment()
    }
}
