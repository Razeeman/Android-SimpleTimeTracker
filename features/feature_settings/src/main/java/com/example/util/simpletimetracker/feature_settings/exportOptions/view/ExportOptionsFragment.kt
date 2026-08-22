package com.example.util.simpletimetracker.feature_settings.exportOptions.view

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseBottomSheetFragment
import com.example.util.simpletimetracker.core.extension.blockContentScroll
import com.example.util.simpletimetracker.core.extension.findListeners
import com.example.util.simpletimetracker.core.extension.setSkipCollapsed
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_settings.api.SettingsBlock
import com.example.util.simpletimetracker.feature_settings.databinding.SettingsExportOptionsFragmentBinding
import com.example.util.simpletimetracker.feature_settings.exportOptions.viewModel.ExportOptionsViewModel
import com.example.util.simpletimetracker.feature_settings.model.AdvancedOptionsBlockClickListener
import com.example.util.simpletimetracker.feature_settings.views.getSettingsAdapterDelegates
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ExportOptionsFragment : BaseBottomSheetFragment<SettingsExportOptionsFragmentBinding>() {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> SettingsExportOptionsFragmentBinding =
        SettingsExportOptionsFragmentBinding::inflate

    private val viewModel: ExportOptionsViewModel by viewModels()

    private val contentAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            *getSettingsAdapterDelegates(
                onBlockClicked = ::onBlockClicked,
                onSpinnerPositionSelected = ::onSpinnerPositionSelected,
            ).toTypedArray(),
        )
    }

    private var listener: AdvancedOptionsBlockClickListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context.findListeners<AdvancedOptionsBlockClickListener>().firstOrNull()
    }

    override fun initDialog() {
        setSkipCollapsed()
        blockContentScroll(binding.rvExportOptionsContent)
    }

    override fun initUi() = with(binding) {
        rvExportOptionsContent.adapter = contentAdapter
        rvExportOptionsContent.itemAnimator = null
    }

    override fun initViewModel() = with(viewModel) {
        listener?.getAdvancedContent()?.observe(contentAdapter::replaceAsNew)
        dismiss.observe { dismiss() }
    }

    private fun onBlockClicked(block: SettingsBlock) {
        listener?.onAdvancedOptionsBlockClicked(block)
    }

    private fun onSpinnerPositionSelected(block: SettingsBlock, position: Int) {
        listener?.onAdvancedOptionsSpinnerPositionSelected(block, position)
    }
}
