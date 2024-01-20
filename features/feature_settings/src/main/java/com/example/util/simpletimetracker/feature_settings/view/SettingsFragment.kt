package com.example.util.simpletimetracker.feature_settings.view

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
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
import com.example.util.simpletimetracker.core.viewData.SettingsBlock
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_settings.adapter.createSettingsBottomAdapterDelegate
import com.example.util.simpletimetracker.feature_settings.adapter.createSettingsCheckboxAdapterDelegate
import com.example.util.simpletimetracker.feature_settings.adapter.createSettingsCheckboxWithRangeAdapterDelegate
import com.example.util.simpletimetracker.feature_settings.adapter.createSettingsCollapseAdapterDelegate
import com.example.util.simpletimetracker.feature_settings.adapter.createSettingsHintAdapterDelegate
import com.example.util.simpletimetracker.feature_settings.adapter.createSettingsRangeAdapterDelegate
import com.example.util.simpletimetracker.feature_settings.adapter.createSettingsSelectorAdapterDelegate
import com.example.util.simpletimetracker.feature_settings.adapter.createSettingsSelectorWithButtonAdapterDelegate
import com.example.util.simpletimetracker.feature_settings.adapter.createSettingsSpinnerAdapterDelegate
import com.example.util.simpletimetracker.feature_settings.adapter.createSettingsSpinnerEvenAdapterDelegate
import com.example.util.simpletimetracker.feature_settings.adapter.createSettingsSpinnerNotCheckableAdapterDelegate
import com.example.util.simpletimetracker.feature_settings.adapter.createSettingsSpinnerWithButtonAdapterDelegate
import com.example.util.simpletimetracker.feature_settings.adapter.createSettingsTextAdapterDelegate
import com.example.util.simpletimetracker.feature_settings.adapter.createSettingsTextWithButtonAdapterDelegate
import com.example.util.simpletimetracker.feature_settings.adapter.createSettingsTopAdapterDelegate
import com.example.util.simpletimetracker.feature_settings.adapter.createSettingsTranslatorAdapterDelegate
import com.example.util.simpletimetracker.feature_settings.viewModel.SettingsViewModel
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

    private val contentAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createSettingsTopAdapterDelegate(),
            createSettingsBottomAdapterDelegate(),
            createSettingsTranslatorAdapterDelegate(),
            createSettingsHintAdapterDelegate(),
            createSettingsTextAdapterDelegate(throttle(::onBlockClicked)),
            createSettingsTextWithButtonAdapterDelegate(::onBlockClicked),
            createSettingsCheckboxAdapterDelegate(::onBlockClicked),
            createSettingsCheckboxWithRangeAdapterDelegate(::onBlockClicked),
            createSettingsCollapseAdapterDelegate(::onBlockClicked),
            createSettingsSelectorAdapterDelegate(::onBlockClicked),
            createSettingsSelectorWithButtonAdapterDelegate(::onBlockClicked),
            createSettingsRangeAdapterDelegate(::onBlockClicked),
            createSettingsSpinnerAdapterDelegate(viewModel::onSpinnerPositionSelected),
            createSettingsSpinnerEvenAdapterDelegate(viewModel::onSpinnerPositionSelected),
            createSettingsSpinnerNotCheckableAdapterDelegate(viewModel::onSpinnerPositionSelected),
            createSettingsSpinnerWithButtonAdapterDelegate(
                onPositionSelected = viewModel::onSpinnerPositionSelected,
                onButtonClicked = ::onBlockClicked,
            ),
        )
    }

    override fun initUi() = with(binding) {
        rvSettingsContent.adapter = contentAdapter
        rvSettingsContent.itemAnimator = null
    }

    override fun initViewModel(): Unit = with(binding) {
        viewModel.content.observe(contentAdapter::replaceAsNew)
        viewModel.resetScreen.observe {
            rvSettingsContent.smoothScrollToPosition(0)
            mainTabsViewModel.onHandled()
        }
        viewModel.mainDelegate.themeChanged.observe(::changeTheme)
        viewModel.displayDelegate.keepScreenOnCheckbox.observe(::setKeepScreenOn)
        backupViewModel.requestScreenUpdate.observe { viewModel.onRequestUpdate() }
        mainTabsViewModel.tabReselected.observe(viewModel::onTabReselected)
    }

    override fun onResume() = with(binding) {
        super.onResume()
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

    private fun onBlockClicked(block: SettingsBlock) {
        viewModel.onBlockClicked(block)
        backupViewModel.onBlockClicked(block)
    }

    private fun setKeepScreenOn(keepScreenOn: Boolean) {
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
