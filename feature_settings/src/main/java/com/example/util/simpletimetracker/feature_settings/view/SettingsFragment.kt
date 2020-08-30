package com.example.util.simpletimetracker.feature_settings.view

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.dialog.StandardDialogListener
import com.example.util.simpletimetracker.core.extension.observeOnce
import com.example.util.simpletimetracker.core.extension.onItemSelected
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

    private var recordTypesSortOrderAdapter: ArrayAdapter<String>? = null

    override fun initDi() {
        (activity?.application as SettingsComponentProvider)
            .settingsComponent
            ?.inject(this)
        tvSettingsVersionName.text = BuildConfig.VERSION_NAME
    }

    override fun initUi() {
        recordTypesSortOrderAdapter = ArrayAdapter(requireContext(), R.layout.item_spinner_layout)
        spinnerSettingsRecordTypeSort.adapter = recordTypesSortOrderAdapter
    }

    override fun initUx() {
        spinnerSettingsRecordTypeSort.onItemSelected(viewModel::onRecordTypeOrderSelected)
        checkboxSettingsShowUntracked.setOnClick(viewModel::onShowUntrackedClicked)
        checkboxSettingsAllowMultitasking.setOnClick(viewModel::onAllowMultitaskingClicked)
        tvSettingsChangeCardSize.setOnClick(viewModel::onChangeCardSizeClick)
        tvSettingsChangeCardOrder.setOnClick(viewModel::onChangeCardOrderClick)
        layoutSettingsSaveBackup.setOnClick(viewModel::onSaveClick)
        tvSettingsRestoreBackup.setOnClick(viewModel::onRestoreClick)
        layoutSettingsRate.setOnClick(viewModel::onRateClick)
        layoutSettingsFeedback.setOnClick(viewModel::onFeedbackClick)
    }

    override fun initViewModel(): Unit = with(viewModel) {
        recordTypesOrderViewData.observeOnce(viewLifecycleOwner) {
            recordTypesSortOrderAdapter?.addAll(it)
        }
        recordTypesOrder.observe(
            viewLifecycleOwner,
            spinnerSettingsRecordTypeSort::setSelection
        )
        showUntrackedCheckbox.observe(
            viewLifecycleOwner,
            checkboxSettingsShowUntracked::setChecked
        )
        allowMultitaskingCheckbox.observe(
            viewLifecycleOwner,
            checkboxSettingsAllowMultitasking::setChecked
        )
    }

    override fun onResume() {
        super.onResume()
        spinnerSettingsRecordTypeSort.jumpDrawablesToCurrentState()
        checkboxSettingsShowUntracked.jumpDrawablesToCurrentState()
        checkboxSettingsAllowMultitasking.jumpDrawablesToCurrentState()
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
