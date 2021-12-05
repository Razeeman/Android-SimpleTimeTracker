package com.example.util.simpletimetracker.feature_statistics_detail.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.hint.createHintAdapterDelegate
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.dialog.CustomRangeSelectionDialogListener
import com.example.util.simpletimetracker.core.dialog.DateTimeDialogListener
import com.example.util.simpletimetracker.core.dialog.TypesFilterDialogListener
import com.example.util.simpletimetracker.core.extension.setSharedTransitions
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_views.extension.setOnLongClick
import com.example.util.simpletimetracker.core.extension.toViewData
import com.example.util.simpletimetracker.core.utils.fragmentArgumentDelegate
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.core.viewData.RangesViewData
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.feature_base_adapter.statisticsTag.createStatisticsTagAdapterDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.createStatisticsPreviewAdapterDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.createStatisticsPreviewMoreAdapterDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailChartCompositeViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailChartViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailPreviewViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailStatsViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewModel.StatisticsDetailViewModel
import com.example.util.simpletimetracker.navigation.params.screen.StatisticsDetailParams
import com.example.util.simpletimetracker.navigation.params.screen.TypesFilterParams
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.example.util.simpletimetracker.feature_statistics_detail.databinding.StatisticsDetailFragmentBinding as Binding

@AndroidEntryPoint
class StatisticsDetailFragment :
    BaseFragment<Binding>(),
    DateTimeDialogListener,
    CustomRangeSelectionDialogListener,
    TypesFilterDialogListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory<StatisticsDetailViewModel>

    private val viewModel: StatisticsDetailViewModel by viewModels(
        factoryProducer = { viewModelFactory }
    )
    private val previewAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createStatisticsPreviewMoreAdapterDelegate(),
            createStatisticsPreviewAdapterDelegate()
        )
    }
    private val tagSplitAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createHintAdapterDelegate(),
            createStatisticsTagAdapterDelegate()
        )
    }
    private val params: StatisticsDetailParams by fragmentArgumentDelegate(
        key = ARGS_PARAMS, default = StatisticsDetailParams()
    )

    override fun initUi(): Unit = with(binding) {
        setPreview()

        setSharedTransitions(
            transitionName = params.transitionName,
            sharedView = viewStatisticsDetailItem,
        )

        rvStatisticsDetailPreviewItems.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.FLEX_START
                flexWrap = FlexWrap.WRAP
            }
            adapter = previewAdapter
        }
        rvStatisticsDetailTagSplit.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = tagSplitAdapter
        }
    }

    override fun initUx() = with(binding) {
        cardStatisticsDetailFilter.setOnClick(viewModel::onFilterClick)
        buttonsStatisticsDetailGrouping.listener = viewModel::onChartGroupingClick
        buttonsStatisticsDetailLength.listener = viewModel::onChartLengthClick
        buttonsStatisticsDetailSplitGrouping.listener = viewModel::onSplitChartGroupingClick
        cardStatisticsDetailRecords.listener = viewModel::onRecordsClick
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
        extra = params

        previewViewData.observe(::setPreviewViewData)
        statsViewData.observe(::setStatsViewData)
        chartViewData.observe(::updateChartViewData)
        splitChartViewData.observe(::updateSplitChartViewData)
        splitChartGroupingViewData.observe(::updateSplitChartGroupingData)
        title.observe(binding.btnStatisticsDetailToday::setText)
        rangeItems.observe(::updateRangeItems)
        rangeButtonsVisibility.observe(::updateRangeButtonsVisibility)
    }

    override fun onResume() {
        super.onResume()
        viewModel.onVisible()
    }

    override fun onTypesFilterSelected(filter: TypesFilterParams) {
        viewModel.onTypesFilterSelected(filter)
    }

    override fun onTypesFilterDismissed() {
        viewModel.onTypesFilterDismissed()
    }

    private fun setPreview() = params.preview?.run {
        StatisticsDetailPreviewViewData(
            id = 0L,
            name = name,
            iconId = iconId?.toViewData(),
            color = color
        ).let(::listOf).let(::setPreviewViewData)
    }

    private fun setPreviewViewData(viewData: List<ViewHolderType>) = with(binding) {
        val first = viewData.firstOrNull()
            as? StatisticsDetailPreviewViewData
            ?: return
        val rest = viewData.drop(1)

        viewStatisticsDetailItem.itemName = first.name
        viewStatisticsDetailItem.itemColor = first.color
        chartStatisticsDetail.setBarColor(first.color)
        chartStatisticsDetailSplit.setBarColor(first.color)
        if (first.iconId != null) {
            viewStatisticsDetailItem.itemIconVisible = true
            viewStatisticsDetailItem.itemIcon = first.iconId
        } else {
            viewStatisticsDetailItem.itemIconVisible = false
        }

        previewAdapter.replace(rest)
    }

    private fun setStatsViewData(statsViewData: StatisticsDetailStatsViewData) = with(binding) {
        cardStatisticsDetailTotal.items = statsViewData.totalDuration
        cardStatisticsDetailRecords.items = statsViewData.timesTracked
        cardStatisticsDetailAverage.items = statsViewData.averageRecord
        cardStatisticsDetailDates.items = statsViewData.datesTracked
        rvStatisticsDetailTagSplit.visible = statsViewData.tagSplitData.isNotEmpty()
        tagSplitAdapter.replace(statsViewData.tagSplitData)
    }

    private fun updateChartViewData(viewData: StatisticsDetailChartCompositeViewData) = with(binding) {
        val chartData = viewData.chartData
        chartStatisticsDetail.visible = chartData.visible
        chartStatisticsDetail.setBars(chartData.data)
        chartStatisticsDetail.setLegendTextSuffix(chartData.legendSuffix)
        chartStatisticsDetail.shouldAddLegendToSelectedBar(chartData.addLegendToSelectedBar)
        chartStatisticsDetail.shouldDrawHorizontalLegends(chartData.shouldDrawHorizontalLegends)

        val chartGroupingData = viewData.chartGroupingViewData
        buttonsStatisticsDetailGrouping.visible = chartGroupingData.size > 1
        buttonsStatisticsDetailGrouping.adapter.replace(chartGroupingData)

        val chartLengthData = viewData.chartLengthViewData
        buttonsStatisticsDetailLength.visible = chartLengthData.isNotEmpty()
        buttonsStatisticsDetailLength.adapter.replace(chartLengthData)

        val rangeAveragesData = viewData.rangeAverages
        cardStatisticsDetailRangeAverage.itemsDescription = viewData.rangeAveragesTitle
        cardStatisticsDetailRangeAverage.visible = rangeAveragesData.isNotEmpty()
        cardStatisticsDetailRangeAverage.items = rangeAveragesData
    }

    private fun updateSplitChartGroupingData(viewData: List<ViewHolderType>) = with(binding) {
        buttonsStatisticsDetailSplitGrouping.visible = viewData.isNotEmpty()
        buttonsStatisticsDetailSplitGrouping.adapter.replace(viewData)
    }

    private fun updateSplitChartViewData(viewData: StatisticsDetailChartViewData) = with(binding) {
        chartStatisticsDetailSplit.visible = viewData.visible
        chartStatisticsDetailSplit.setBars(viewData.data)
        chartStatisticsDetailSplit.setLegendTextSuffix(viewData.legendSuffix)
        chartStatisticsDetailSplit.shouldAddLegendToSelectedBar(viewData.addLegendToSelectedBar)
        chartStatisticsDetailSplit.shouldDrawHorizontalLegends(viewData.shouldDrawHorizontalLegends)
    }

    private fun updateRangeItems(viewData: RangesViewData) = with(binding) {
        spinnerStatisticsDetail.setData(viewData.items, viewData.selectedPosition)
    }

    private fun updateRangeButtonsVisibility(isVisible: Boolean) = with(binding) {
        btnStatisticsDetailPrevious.visible = isVisible
        btnStatisticsDetailNext.visible = isVisible
    }

    companion object {
        private const val ARGS_PARAMS = "args_params"

        fun createBundle(data: StatisticsDetailParams): Bundle = Bundle().apply {
            putParcelable(ARGS_PARAMS, data)
        }
    }
}
