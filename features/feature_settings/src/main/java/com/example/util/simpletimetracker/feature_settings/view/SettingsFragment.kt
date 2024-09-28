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
import com.example.util.simpletimetracker.core.dialog.TypesSelectionDialogListener
import com.example.util.simpletimetracker.core.sharedViewModel.MainTabsViewModel
import com.example.util.simpletimetracker.core.utils.InsetConfiguration
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_settings.api.SettingsBlock
import com.example.util.simpletimetracker.feature_settings.viewModel.SettingsViewModel
import com.example.util.simpletimetracker.feature_settings.views.getSettingsAdapterDelegates
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
    DataExportSettingsDialogListener,
    TypesSelectionDialogListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    override var insetConfiguration: InsetConfiguration =
        InsetConfiguration.DoNotApply

    @Inject
    lateinit var mainTabsViewModelFactory: BaseViewModelFactory<MainTabsViewModel>

    private val viewModel: SettingsViewModel by viewModels()
    private val mainTabsViewModel: MainTabsViewModel by activityViewModels { mainTabsViewModelFactory }

    private val contentAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            *getSettingsAdapterDelegates(
                onBlockClicked = ::onBlockClicked,
                onBlockClickedThrottled = throttle(::onBlockClicked),
                onSpinnerPositionSelected = viewModel::onSpinnerPositionSelected,
            ).toTypedArray(),
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
        viewModel.themeChanged.observe(::changeTheme)
        viewModel.keepScreenOnCheckbox.observe(::setKeepScreenOn)
        with(mainTabsViewModel) {
            tabReselected.observe(viewModel::onTabReselected)
            isNavBatAtTheBottom.observe(::updateInsetConfiguration)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onVisible()
    }

    override fun onPause() {
        viewModel.onHidden()
        super.onPause()
    }

    override fun onPositiveClick(tag: String?, data: Any?) {
        viewModel.onPositiveClick(tag)
    }

    override fun onDurationSet(durationSeconds: Long, tag: String?) {
        viewModel.onDurationSet(tag, durationSeconds)
    }

    override fun onDisable(tag: String?) {
        viewModel.onDurationDisabled(tag)
    }

    override fun onDateTimeSet(timestamp: Long, tag: String?) {
        viewModel.onDateTimeSet(timestamp, tag)
    }

    override fun onDataExportSettingsSelected(data: DataExportSettingsResult) {
        viewModel.onDataExportSettingsSelected(data)
    }

    override fun onDataSelected(dataIds: List<Long>, tag: String?) {
        viewModel.onTypesSelected(dataIds, tag)
    }

    private fun onBlockClicked(block: SettingsBlock) {
        viewModel.onBlockClicked(block)
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

    private fun updateInsetConfiguration(isNavBatAtTheBottom: Boolean) {
        insetConfiguration = if (isNavBatAtTheBottom) {
            InsetConfiguration.DoNotApply
        } else {
            InsetConfiguration.ApplyToView { binding.rvSettingsContent }
        }
        initInsets()
    }

    companion object {
        fun newInstance(): SettingsFragment = SettingsFragment()
    }
}
