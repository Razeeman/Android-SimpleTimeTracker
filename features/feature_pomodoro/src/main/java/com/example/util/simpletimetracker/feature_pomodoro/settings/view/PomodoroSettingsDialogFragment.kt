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
import com.example.util.simpletimetracker.feature_settings_views.getSettingsAdapterDelegates
import dagger.hilt.android.AndroidEntryPoint
import com.example.util.simpletimetracker.feature_pomodoro.databinding.PomodoroSettingsFragmentBinding as Binding

@AndroidEntryPoint
class PomodoroSettingsDialogFragment :
    BaseBottomSheetFragment<Binding>(),
    DurationDialogListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    private val viewModel: PomodoroSettingsViewModel by viewModels()

    private val contentAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createHintBigAdapterDelegate(
                onActionClick = viewModel::onHintActionClicked,
            ),
            *getSettingsAdapterDelegates(
                onBlockClicked = viewModel::onBlockClicked,
                onSpinnerPositionSelected = viewModel::onSpinnerPositionSelected,
            ).toTypedArray(),
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
