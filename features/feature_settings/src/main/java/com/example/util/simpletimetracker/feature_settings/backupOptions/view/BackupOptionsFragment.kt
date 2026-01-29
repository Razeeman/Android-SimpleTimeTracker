package com.example.util.simpletimetracker.feature_settings.backupOptions.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseBottomSheetFragment
import com.example.util.simpletimetracker.core.dialog.StandardDialogListener
import com.example.util.simpletimetracker.core.extension.blockContentScroll
import com.example.util.simpletimetracker.core.extension.setSkipCollapsed
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_settings.backupOptions.viewModel.BackupOptionsViewModel
import com.example.util.simpletimetracker.feature_settings.views.getSettingsAdapterDelegates
import dagger.hilt.android.AndroidEntryPoint
import com.example.util.simpletimetracker.feature_settings.databinding.SettingsBackupOptionsFragmentBinding as Binding

@AndroidEntryPoint
class BackupOptionsFragment :
    BaseBottomSheetFragment<Binding>(),
    StandardDialogListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    private val viewModel: BackupOptionsViewModel by viewModels()

    private val contentAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            *getSettingsAdapterDelegates(
                onBlockClicked = viewModel::onBlockClicked,
                onSpinnerPositionSelected = viewModel::onSpinnerPositionSelected,
            ).toTypedArray(),
        )
    }

    override fun initDialog() {
        setSkipCollapsed()
        blockContentScroll(binding.rvBackupOptionsContent)
    }

    override fun initUi() = with(binding) {
        rvBackupOptionsContent.adapter = contentAdapter
        rvBackupOptionsContent.itemAnimator = null
    }

    override fun initViewModel() = with(viewModel) {
        viewModel.content.observe(contentAdapter::replaceAsNew)
        dismiss.observe { dismiss() }
    }

    override fun onPositiveClick(tag: String?, data: Any?) {
        viewModel.onPositiveClick(tag)
    }
}
