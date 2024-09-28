package com.example.util.simpletimetracker.feature_settings.backupOptions.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseBottomSheetFragment
import com.example.util.simpletimetracker.core.dialog.StandardDialogListener
import com.example.util.simpletimetracker.core.extension.setSkipCollapsed
import com.example.util.simpletimetracker.feature_settings.backupOptions.viewModel.BackupOptionsViewModel
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import dagger.hilt.android.AndroidEntryPoint
import com.example.util.simpletimetracker.feature_settings.databinding.SettingsBackupOptionsFragmentBinding as Binding

@AndroidEntryPoint
class BackupOptionsFragment :
    BaseBottomSheetFragment<Binding>(),
    StandardDialogListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    private val viewModel: BackupOptionsViewModel by viewModels()

    override fun initDialog() {
        setSkipCollapsed()
    }

    override fun initUx() = with(binding) {
        layoutBackupOptionsPartialSave.setOnClick(throttle(viewModel::onPartialSaveClick))
        layoutBackupOptionsFullRestore.setOnClick(throttle(viewModel::onFullRestoreClick))
        layoutBackupOptionsPartialRestore.setOnClick(throttle(viewModel::onPartialRestoreClick))
    }

    override fun initViewModel() = with(viewModel) {
        dismiss.observe { dismiss() }
    }

    override fun onPositiveClick(tag: String?, data: Any?) {
        viewModel.onPositiveClick(tag)
    }
}
