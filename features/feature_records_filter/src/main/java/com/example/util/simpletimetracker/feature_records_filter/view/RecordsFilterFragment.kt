package com.example.util.simpletimetracker.feature_records_filter.view

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.util.simpletimetracker.core.base.BaseBottomSheetFragment
import com.example.util.simpletimetracker.feature_dialogs.api.DateTimeDialogListener
import com.example.util.simpletimetracker.feature_dialogs.api.DurationDialogListener
import com.example.util.simpletimetracker.feature_dialogs.api.RecordsFilterListener
import com.example.util.simpletimetracker.feature_dialogs.api.StandardDialogListener
import com.example.util.simpletimetracker.core.extension.blockContentScroll
import com.example.util.simpletimetracker.core.extension.findListeners
import com.example.util.simpletimetracker.core.extension.hideKeyboard
import com.example.util.simpletimetracker.core.extension.setFullScreen
import com.example.util.simpletimetracker.core.extension.setSkipCollapsed
import com.example.util.simpletimetracker.core.utils.fragmentArgumentDelegate
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.buttonDouble.createDoubleButtonsAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.category.createCategoryAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.divider.createDividerAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.empty.createEmptyAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.hint.createHintAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.loader.createLoaderAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.multitaskRecord.createMultitaskRecordAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.record.createRecordAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.recordFilter.createFilterAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.recordType.createRecordTypeAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.recordsDateDivider.createRecordsDateDividerAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.runningRecord.createRunningRecordAdapterDelegate
import com.example.util.simpletimetracker.feature_records_filter.adapter.createRecordsFilterButtonAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.commentField.createCommentFieldAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.dayOfWeek.createDayOfWeekAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.emptySpace.createEmptySpaceAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.recordTypeSuggestion.createRecordTypeSuggestionAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.recordsFilter.createFavouriteRecordsFilterAdapterDelegate
import com.example.util.simpletimetracker.feature_records_filter.adapter.createRecordsFilterRangeAdapterDelegate
import com.example.util.simpletimetracker.feature_records_filter.model.RecordsFilterSelectedRecordsViewData
import com.example.util.simpletimetracker.feature_records_filter.viewData.RecordTypeFilteredType
import com.example.util.simpletimetracker.feature_records_filter.viewModel.RecordsFilterViewModel
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.navigation.params.screen.ARGS_PARAMS
import com.example.util.simpletimetracker.navigation.params.screen.RecordsFilterParams
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import com.example.util.simpletimetracker.feature_records_filter.databinding.RecordsFilterFragmentBinding as Binding

@AndroidEntryPoint
class RecordsFilterFragment :
    BaseBottomSheetFragment<Binding>(),
    DateTimeDialogListener,
    DurationDialogListener,
    StandardDialogListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    private val viewModel: RecordsFilterViewModel by viewModels()

    private val filtersAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createLoaderAdapterDelegate(),
            createFilterAdapterDelegate(
                onClick = viewModel::onFilterClick,
                onButtonClick = viewModel::onFilterRemoveClick,
            ),
        )
    }
    private val filterSelectionAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createLoaderAdapterDelegate(),
            createHintAdapterDelegate(),
            createDividerAdapterDelegate(),
            createEmptySpaceAdapterDelegate(),
            createRecordsDateDividerAdapterDelegate(),
            createRecordTypeAdapterDelegate(viewModel::onRecordTypeClick),
            createRecordTypeSuggestionAdapterDelegate(
                type = RecordTypeFilteredType,
                onItemClick = viewModel::onFilteredRecordTypeClick,
                withTransition = false,
            ),
            createCategoryAdapterDelegate(viewModel::onCategoryClick),
            createRunningRecordAdapterDelegate(
                transitionNamePrefix = "",
                onItemClick = viewModel::onRunningRecordClick,
            ),
            createRecordAdapterDelegate(
                onItemClick = viewModel::onRecordClick,
            ),
            createMultitaskRecordAdapterDelegate(
                onItemClick = viewModel::onMultitaskRecordClick,
            ),
            createFavouriteRecordsFilterAdapterDelegate(viewModel::onFavouriteFilterClick),
            createDoubleButtonsAdapterDelegate(viewModel::onSelectionButtonClick),
            createCommentFieldAdapterDelegate(viewModel::onCommentChange),
            createRecordsFilterButtonAdapterDelegate(viewModel::onInnerFilterButtonClick),
            createDayOfWeekAdapterDelegate(viewModel::onDayOfWeekClick),
            createRecordsFilterRangeAdapterDelegate(viewModel::onRangeTimeClick),
            createFilterAdapterDelegate(viewModel::onInnerFilterClick),
        )
    }
    private val recordsAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createLoaderAdapterDelegate(),
            createEmptyAdapterDelegate(),
            createRecordsDateDividerAdapterDelegate(),
            createRunningRecordAdapterDelegate(
                transitionNamePrefix = "",
                onItemClick = viewModel::onRunningRecordClick,
            ),
            createRecordAdapterDelegate(
                onItemClick = viewModel::onRecordClick,
            ),
            createMultitaskRecordAdapterDelegate(
                onItemClick = viewModel::onMultitaskRecordClick,
            ),
        )
    }
    private val params: RecordsFilterParams by fragmentArgumentDelegate(
        key = ARGS_PARAMS, default = RecordsFilterParams.Empty,
    )
    private var listener: RecordsFilterListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context.findListeners<RecordsFilterListener>().firstOrNull()
    }

    override fun initDialog() {
        setSkipCollapsed()
        setFullScreen()
        blockContentScroll(binding.rvRecordsFilterList)
        blockContentScroll(binding.rvRecordsFilterSelection)
    }

    override fun initUi(): Unit = with(binding) {
        rvRecordsFilterFilters.isNestedScrollingEnabled = false
        rvRecordsFilterFilters.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = filtersAdapter
        }
        rvRecordsFilterSelection.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = filterSelectionAdapter
        }
        rvRecordsFilterList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recordsAdapter
        }
    }

    override fun initUx() = with(binding) {
        ivRecordsFilterShowList.setOnClick(viewModel::onShowRecordsListClick)
    }

    override fun initViewModel(): Unit = with(viewModel) {
        init(params)
        filtersViewData.observe(filtersAdapter::replace)
        filterSelectionContent.observe(filterSelectionAdapter::replace)
        recordsViewData.observe(::setSelectedRecords)
        filterSelectionVisibility.observe(::setFilterSelectionVisibility)
        keyboardVisibility.observe(::showKeyboard)
        changedFilters.observe { listener?.onFilterChanged(it) }
    }

    override fun onDismiss(dialog: DialogInterface) {
        listener?.onFilterDismissed(params.tag)
        super.onDismiss(dialog)
    }

    override fun onPositiveClick(tag: String?, data: Any?) {
        viewModel.onPositiveDialogClick(tag, data)
    }

    override fun onDateTimeSet(timestamp: Long, tag: String?) {
        viewModel.onDateTimeSet(timestamp, tag)
    }

    override fun onDurationSet(durationSeconds: Long, tag: String?) {
        viewModel.onDurationSet(durationSeconds, tag)
    }

    private fun setFilterSelectionVisibility(filterOpened: Boolean) = with(binding) {
        rvRecordsFilterList.isVisible = !filterOpened
        rvRecordsFilterList.isNestedScrollingEnabled = !filterOpened
        rvRecordsFilterSelection.isVisible = filterOpened
        rvRecordsFilterSelection.isNestedScrollingEnabled = filterOpened
    }

    private fun setSelectedRecords(viewData: RecordsFilterSelectedRecordsViewData) = with(binding) {
        loaderRecordsFilter.isVisible = viewData.isLoading
        tvRecordsFilterTitle.isInvisible = viewData.isLoading
        tvRecordsFilterTitle.text = viewData.selectedRecordsCount
        ivRecordsFilterShowList.isVisible = !viewData.isLoading && viewData.showListButtonIsVisible
        when (viewData.recordsViewData) {
            is RecordsFilterSelectedRecordsViewData.RecordsViewData.Loading -> {
                recordsAdapter.replaceFast(viewData.recordsViewData.viewData)
            }
            is RecordsFilterSelectedRecordsViewData.RecordsViewData.Content -> {
                recordsAdapter.replace(viewData.recordsViewData.viewData)
            }
        }
    }

    private fun showKeyboard(visible: Boolean) {
        if (visible) {
            // Do nothing.
        } else {
            hideKeyboard()
        }
    }

    companion object {
        fun createBundle(data: RecordsFilterParams): Bundle = Bundle().apply {
            putParcelable(ARGS_PARAMS, data)
        }
    }
}
