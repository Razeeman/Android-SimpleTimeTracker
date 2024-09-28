package com.example.util.simpletimetracker.feature_settings.partialRestore.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseBottomSheetFragment
import com.example.util.simpletimetracker.core.extension.setFullScreen
import com.example.util.simpletimetracker.core.extension.setSkipCollapsed
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.recordFilter.createFilterAdapterDelegate
import com.example.util.simpletimetracker.feature_settings.partialRestore.model.PartialRestoreFilterType
import com.example.util.simpletimetracker.feature_settings.partialRestore.viewModel.PartialRestoreViewModel
import com.example.util.simpletimetracker.feature_settings.partialRestoreSelection.model.PartialRestoreSelectionDialogListener
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import com.example.util.simpletimetracker.feature_settings.databinding.SettingsPartialRestoreFragmentBinding as Binding

@AndroidEntryPoint
class PartialRestoreFragment :
    BaseBottomSheetFragment<Binding>(),
    PartialRestoreSelectionDialogListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    private val viewModel: PartialRestoreViewModel by viewModels()

    private val filtersAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createFilterAdapterDelegate(
                onClick = viewModel::onFilterClick,
                onRemoveClick = {
                    viewModel.onFilterRemoveClick(it)
                },
            ),
        )
    }

    override fun initDialog() {
        setSkipCollapsed()
    }

    override fun initUi(): Unit = with(binding) {
        rvPartialRestoreFilters.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = filtersAdapter
        }
    }

    override fun initUx() = with(binding) {
        layoutPartialRestoreButton.setOnClick(viewModel::onRestoreClick)
    }

    override fun initViewModel() = with(viewModel) {
        filtersViewData.observe(filtersAdapter::replace)
        dismiss.observe { dismiss() }
    }

    override fun onDataSelected(
        tag: String?,
        type: PartialRestoreFilterType,
        dataIds: Set<Long>,
    ) {
        viewModel.onDataSelected(type, dataIds, tag)
    }
}
