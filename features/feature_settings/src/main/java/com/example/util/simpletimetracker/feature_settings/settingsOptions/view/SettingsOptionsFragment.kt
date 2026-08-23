package com.example.util.simpletimetracker.feature_settings.settingsOptions.view

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseBottomSheetFragment
import com.example.util.simpletimetracker.core.extension.blockContentScroll
import com.example.util.simpletimetracker.core.extension.findListeners
import com.example.util.simpletimetracker.core.extension.setSkipCollapsed
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.dayOfWeek.DayOfWeekViewData
import com.example.util.simpletimetracker.feature_settings.api.SettingsBlock
import com.example.util.simpletimetracker.feature_settings.databinding.SettingsOptionsFragmentBinding
import com.example.util.simpletimetracker.feature_settings.model.SettingsOptionsBlockClickListener
import com.example.util.simpletimetracker.feature_settings.settingsOptions.viewModel.SettingsOptionsViewModel
import com.example.util.simpletimetracker.feature_settings.views.getSettingsAdapterDelegates
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsOptionsFragment : BaseBottomSheetFragment<SettingsOptionsFragmentBinding>() {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> SettingsOptionsFragmentBinding =
        SettingsOptionsFragmentBinding::inflate

    private val viewModel: SettingsOptionsViewModel by viewModels()

    private val contentAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            *getSettingsAdapterDelegates(
                onBlockClicked = ::onBlockClicked,
                onSpinnerPositionSelected = ::onSpinnerPositionSelected,
                onDayOfWeekClick = ::onDayOfWeekClicked,
            ).toTypedArray(),
        )
    }

    private var listener: SettingsOptionsBlockClickListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context.findListeners<SettingsOptionsBlockClickListener>().firstOrNull()
    }

    override fun initDialog() {
        setSkipCollapsed()
        blockContentScroll(binding.rvSettingsOptionsContent)
    }

    override fun initUi() = with(binding) {
        rvSettingsOptionsContent.adapter = contentAdapter
        rvSettingsOptionsContent.itemAnimator = null
    }

    override fun initViewModel() = with(viewModel) {
        listener?.getOptionsContent()?.observe(contentAdapter::replaceAsNew)
        dismiss.observe { dismiss() }
    }

    private fun onBlockClicked(block: SettingsBlock) {
        listener?.onOptionsBlockClicked(block)
    }

    private fun onSpinnerPositionSelected(block: SettingsBlock, position: Int) {
        listener?.onOptionsSpinnerPositionSelected(block, position)
    }

    private fun onDayOfWeekClicked(data: DayOfWeekViewData) {
        listener?.onOptionsDayOfWeekClicked(data)
    }
}
