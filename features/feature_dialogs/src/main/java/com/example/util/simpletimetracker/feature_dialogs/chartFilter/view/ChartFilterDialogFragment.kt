package com.example.util.simpletimetracker.feature_dialogs.chartFilter.view

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseBottomSheetFragment
import com.example.util.simpletimetracker.feature_dialogs.api.ChartFilterDialogListener
import com.example.util.simpletimetracker.core.extension.blockContentScroll
import com.example.util.simpletimetracker.core.extension.findListener
import com.example.util.simpletimetracker.core.extension.setSkipCollapsed
import com.example.util.simpletimetracker.core.utils.fragmentArgumentDelegate
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.category.createCategoryAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.empty.createEmptyAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.loader.createLoaderAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.recordType.createRecordTypeAdapterDelegate
import com.example.util.simpletimetracker.feature_dialogs.chartFilter.model.ChartFilterDataSelectionResult
import com.example.util.simpletimetracker.feature_dialogs.chartFilter.viewModel.ChartFilterViewModel
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.navigation.params.screen.ARGS_PARAMS
import com.example.util.simpletimetracker.navigation.params.screen.ChartFilterDialogParams
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import com.example.util.simpletimetracker.feature_dialogs.databinding.ChartFilterDialogFragmentBinding as Binding

@AndroidEntryPoint
class ChartFilterDialogFragment : BaseBottomSheetFragment<Binding>() {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    private val viewModel: ChartFilterViewModel by viewModels()

    private val recordTypesAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createRecordTypeAdapterDelegate(viewModel::onRecordTypeClick),
            createCategoryAdapterDelegate(viewModel::onCategoryClick),
            createLoaderAdapterDelegate(),
            createEmptyAdapterDelegate(),
        )
    }
    private val params: ChartFilterDialogParams by fragmentArgumentDelegate(
        key = ARGS_PARAMS, default = ChartFilterDialogParams.Empty,
    )
    private var listener: ChartFilterDialogListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context.findListener()
        listener?.onChartFilterDialogOpened()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        listener?.onChartFilterDialogDismissed()
    }

    override fun initDialog() {
        setSkipCollapsed()
        blockContentScroll(binding.rvChartFilterContainer)
    }

    override fun initUi() {
        binding.rvChartFilterContainer.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = recordTypesAdapter
        }
    }

    override fun initUx(): Unit = with(binding) {
        buttonsChartFilterType.listener = viewModel::onFilterTypeClick
        btnChartFilterShowAll.setOnClick(viewModel::onShowAllClick)
        btnChartFilterHideAll.setOnClick(viewModel::onHideAllClick)
    }

    override fun initViewModel(): Unit = with(viewModel) {
        extra = params
        filterTypeViewData.observe(binding.buttonsChartFilterType::replace)
        types.observe(::setViewData)
        onDataSelected.observe(::onDataSelected)
    }

    private fun setViewData(data: List<ViewHolderType>) = with(binding) {
        val current = recordTypesAdapter.currentList
        recordTypesAdapter.replace(data)
        // Flexbox is collapsing to zero height when it is scrolled to bottom
        // and data is replaces with shorter list.
        // Scroll to top to prevent this.
        if (data.size < current.size) {
            rvChartFilterContainer.scrollToPosition(0)
        }
    }

    private fun onDataSelected(result: ChartFilterDataSelectionResult) {
        listener?.onChartFilterDataSelected(
            chartFilterType = result.chartFilterType,
            dataIds = result.dataIds,
        )
    }

    companion object {
        fun createBundle(data: ChartFilterDialogParams): Bundle = Bundle().apply {
            putParcelable(ARGS_PARAMS, data)
        }
    }
}