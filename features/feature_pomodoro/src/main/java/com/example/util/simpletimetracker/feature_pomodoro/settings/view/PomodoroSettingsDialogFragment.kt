package com.example.util.simpletimetracker.feature_pomodoro.settings.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseBottomSheetFragment
import com.example.util.simpletimetracker.core.dialog.DurationDialogListener
import com.example.util.simpletimetracker.core.extension.blockContentScroll
import com.example.util.simpletimetracker.core.extension.setSkipCollapsed
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.hintBig.createHintBigAdapterDelegate
import com.example.util.simpletimetracker.feature_pomodoro.settings.viewModel.PomodoroSettingsViewModel
import com.example.util.simpletimetracker.feature_settings.adapter.createSettingsBottomAdapterDelegate
import com.example.util.simpletimetracker.feature_settings.adapter.createSettingsCheckboxAdapterDelegate
import com.example.util.simpletimetracker.feature_settings.adapter.createSettingsCheckboxWithButtonAdapterDelegate
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
import dagger.hilt.android.AndroidEntryPoint
import com.example.util.simpletimetracker.feature_pomodoro.databinding.PomodoroSettingsFragmentBinding as Binding

@AndroidEntryPoint
class PomodoroSettingsDialogFragment :
    BaseBottomSheetFragment<Binding>(),
    DurationDialogListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    private val viewModel: PomodoroSettingsViewModel by viewModels()

    // TODO remove duplicate with settings
    private val contentAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createHintBigAdapterDelegate(onActionClick = viewModel::onHintActionClicked),
            createSettingsTopAdapterDelegate(),
            createSettingsBottomAdapterDelegate(),
            createSettingsTranslatorAdapterDelegate(),
            createSettingsHintAdapterDelegate(),
            createSettingsTextAdapterDelegate(viewModel::onBlockClicked),
            createSettingsTextWithButtonAdapterDelegate(viewModel::onBlockClicked),
            createSettingsCheckboxAdapterDelegate(viewModel::onBlockClicked),
            createSettingsCheckboxWithButtonAdapterDelegate(viewModel::onBlockClicked),
            createSettingsCheckboxWithRangeAdapterDelegate(viewModel::onBlockClicked),
            createSettingsCollapseAdapterDelegate(viewModel::onBlockClicked),
            createSettingsSelectorAdapterDelegate(viewModel::onBlockClicked),
            createSettingsSelectorWithButtonAdapterDelegate(viewModel::onBlockClicked),
            createSettingsRangeAdapterDelegate(viewModel::onBlockClicked),
            createSettingsSpinnerAdapterDelegate(viewModel::onSpinnerPositionSelected),
            createSettingsSpinnerEvenAdapterDelegate(viewModel::onSpinnerPositionSelected),
            createSettingsSpinnerNotCheckableAdapterDelegate(viewModel::onSpinnerPositionSelected),
            createSettingsSpinnerWithButtonAdapterDelegate(
                onPositionSelected = viewModel::onSpinnerPositionSelected,
                onButtonClicked = viewModel::onBlockClicked,
            ),
        )
    }

    override fun initDialog() {
        setSkipCollapsed()
        blockContentScroll(binding.rvPomodoroSettingsContent)
    }

    override fun initUi() = with(binding) {
        rvPomodoroSettingsContent.adapter = contentAdapter
        rvPomodoroSettingsContent.itemAnimator = null
    }

    override fun initViewModel() {
        viewModel.content.observe(contentAdapter::replaceAsNew)
    }

    override fun onResume() {
        super.onResume()
        viewModel.onVisible()
    }

    override fun onDurationSet(duration: Long, tag: String?) {
        viewModel.onDurationSet(tag, duration)
    }

    override fun onCountSet(count: Long, tag: String?) {
        viewModel.onCountSet(tag, count)
    }

    override fun onDisable(tag: String?) {
        viewModel.onDurationDisabled(tag)
    }
}
