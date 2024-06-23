package com.example.util.simpletimetracker.feature_statistics_detail.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.dialog.CustomRangeSelectionDialogListener
import com.example.util.simpletimetracker.core.dialog.DateTimeDialogListener
import com.example.util.simpletimetracker.core.dialog.RecordsFilterListener
import com.example.util.simpletimetracker.core.extension.setSharedTransitions
import com.example.util.simpletimetracker.core.extension.toViewData
import com.example.util.simpletimetracker.core.utils.fragmentArgumentDelegate
import com.example.util.simpletimetracker.core.viewData.RangesViewData
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.hint.createHintAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.statisticsTag.createStatisticsTagAdapterDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.createStatisticsDetailBarChartAdapterDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.createStatisticsDetailButtonsRowAdapterDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.createStatisticsDetailCardAdapterDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.createStatisticsDetailCardDoubleAdapterDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.createStatisticsDetailHintAdapterDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.createStatisticsDetailNextActivitiesAdapterDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.createStatisticsDetailPreviewsAdapterDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.createStatisticsDetailSeriesCalendarAdapterDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.createStatisticsDetailSeriesChartAdapterDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailPreviewCompositeViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailPreviewViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewModel.StatisticsDetailViewModel
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_views.extension.setOnLongClick
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.navigation.params.screen.RecordsFilterResultParams
import com.example.util.simpletimetracker.navigation.params.screen.StatisticsDetailParams
import dagger.hilt.android.AndroidEntryPoint
import com.example.util.simpletimetracker.feature_statistics_detail.databinding.StatisticsDetailFragmentBinding as Binding

@AndroidEntryPoint
class StatisticsDetailFragment :
    BaseFragment<Binding>(),
    DateTimeDialogListener,
    CustomRangeSelectionDialogListener,
    RecordsFilterListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    private val viewModel: StatisticsDetailViewModel by viewModels()

    private val contentAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createStatisticsDetailPreviewsAdapterDelegate(),
            createStatisticsDetailBarChartAdapterDelegate(),
            createStatisticsDetailButtonsRowAdapterDelegate(
                onClick = viewModel::onButtonsRowClick,
            ),
            createStatisticsDetailCardAdapterDelegate(
                onClick = throttle(viewModel::onCardClick),
            ),
            createStatisticsDetailCardDoubleAdapterDelegate(
                onFirstClick = throttle(viewModel::onCardClick),
                onSecondClick = throttle(viewModel::onCardClick),
            ),
            createStatisticsDetailSeriesChartAdapterDelegate(),
            createStatisticsDetailSeriesCalendarAdapterDelegate(
                onClick = throttle(viewModel::onStreaksCalendarClick),
            ),
            createStatisticsDetailHintAdapterDelegate(),
            createStatisticsDetailNextActivitiesAdapterDelegate(),
            createHintAdapterDelegate(),
            createStatisticsTagAdapterDelegate(),
        )
    }
    private val params: StatisticsDetailParams by fragmentArgumentDelegate(
        key = ARGS_PARAMS, default = StatisticsDetailParams(),
    )

    override fun initUi(): Unit = with(binding) {
        setPreview()

        setSharedTransitions(
            additionalCondition = { params.transitionName.isNotEmpty() },
            transitionName = params.transitionName,
            sharedView = viewStatisticsDetailItem,
        )

        rvStatisticsDetailContent.adapter = contentAdapter
    }

    override fun initUx() = with(binding) {
        cardStatisticsDetailFilter.setOnClick(viewModel::onFilterClick)
        cardStatisticsDetailCompare.setOnClick(viewModel::onCompareClick)
        spinnerStatisticsDetail.onItemSelected = viewModel::onRangeSelected
        btnStatisticsDetailPrevious.setOnClick(viewModel::onPreviousClick)
        btnStatisticsDetailNext.setOnClick(viewModel::onNextClick)
        btnStatisticsDetailToday.setOnClick { spinnerStatisticsDetail.performClick() }
        btnStatisticsDetailToday.setOnLongClick(viewModel::onTodayClick)
    }

    override fun onDateTimeSet(timestamp: Long, tag: String?) {
        viewModel.onDateTimeSet(timestamp, tag)
    }

    override fun onCustomRangeSelected(range: Range) {
        viewModel.onCustomRangeSelected(range)
    }

    override fun initViewModel(): Unit = with(viewModel) {
        initialize(params)

        // TODO expand appbar on short list.
        scrollToTop.observe { scrollToTop() }
        content.observe(contentAdapter::replace)
        previewViewData.observe(::setPreviewViewData)
        title.observe(binding.btnStatisticsDetailToday::setText)
        rangeItems.observe(::updateRangeItems)
        rangeButtonsVisibility.observe(::updateRangeButtonsVisibility)
    }

    override fun onResume() {
        super.onResume()
        viewModel.onVisible()
    }

    override fun onFilterChanged(result: RecordsFilterResultParams) {
        viewModel.onTypesFilterSelected(result)
    }

    override fun onFilterDismissed(tag: String) {
        viewModel.onTypesFilterDismissed(tag)
    }

    private fun setPreview() = params.preview?.run {
        val preview = StatisticsDetailPreviewViewData(
            id = 0L,
            type = StatisticsDetailPreviewViewData.Type.FILTER,
            name = name,
            iconId = iconId?.toViewData(),
            color = color,
        )

        StatisticsDetailPreviewCompositeViewData(
            data = preview,
            additionalData = emptyList(),
            comparisonData = emptyList(),
        ).let(::setPreviewViewData)
    }

    private fun setPreviewViewData(viewData: StatisticsDetailPreviewCompositeViewData?) = with(binding) {
        val first = viewData?.data ?: return@with

        viewStatisticsDetailItem.itemName = first.name
        viewStatisticsDetailItem.itemColor = first.color
        if (first.iconId != null) {
            viewStatisticsDetailItem.itemIconVisible = true
            viewStatisticsDetailItem.itemIcon = first.iconId
        } else {
            viewStatisticsDetailItem.itemIconVisible = false
        }
    }

    private fun updateRangeItems(viewData: RangesViewData) = with(binding) {
        spinnerStatisticsDetail.setData(viewData.items, viewData.selectedPosition)
    }

    private fun updateRangeButtonsVisibility(isVisible: Boolean) = with(binding) {
        btnStatisticsDetailPrevious.visible = isVisible
        btnStatisticsDetailNext.visible = isVisible
    }

    private fun scrollToTop() {
        binding.appBarStatisticsDetail.setExpanded(true)
        binding.rvStatisticsDetailContent.apply { post { smoothScrollToPosition(0) } }
    }

    companion object {
        private const val ARGS_PARAMS = "args_params"

        fun createBundle(data: StatisticsDetailParams): Bundle = Bundle().apply {
            putParcelable(ARGS_PARAMS, data)
        }
    }
}
