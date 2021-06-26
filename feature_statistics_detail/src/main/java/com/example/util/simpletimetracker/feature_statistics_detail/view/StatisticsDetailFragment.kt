package com.example.util.simpletimetracker.feature_statistics_detail.view

import android.os.Bundle
import androidx.core.view.ViewCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionInflater
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.adapter.hint.createHintAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.statistics.createStatisticsAdapterDelegate
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.dialog.DateTimeDialogListener
import com.example.util.simpletimetracker.core.dialog.TypesFilterDialogListener
import com.example.util.simpletimetracker.core.extension.setOnClick
import com.example.util.simpletimetracker.core.extension.setOnLongClick
import com.example.util.simpletimetracker.core.extension.toViewData
import com.example.util.simpletimetracker.core.extension.visible
import com.example.util.simpletimetracker.core.utils.BuildVersions
import com.example.util.simpletimetracker.core.viewData.RangesViewData
import com.example.util.simpletimetracker.feature_statistics_detail.R
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.createStatisticsPreviewAdapterDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.createStatisticsPreviewMoreAdapterDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailChartViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailPreviewViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailStatsViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewModel.StatisticsDetailViewModel
import com.example.util.simpletimetracker.navigation.params.StatisticsDetailParams
import com.example.util.simpletimetracker.navigation.params.TypesFilterParams
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.statistics_detail_fragment.btnStatisticsDetailNext
import kotlinx.android.synthetic.main.statistics_detail_fragment.btnStatisticsDetailPrevious
import kotlinx.android.synthetic.main.statistics_detail_fragment.btnStatisticsDetailToday
import kotlinx.android.synthetic.main.statistics_detail_fragment.buttonsStatisticsDetailGrouping
import kotlinx.android.synthetic.main.statistics_detail_fragment.buttonsStatisticsDetailLength
import kotlinx.android.synthetic.main.statistics_detail_fragment.buttonsStatisticsDetailSplitGrouping
import kotlinx.android.synthetic.main.statistics_detail_fragment.cardStatisticsDetailAverage
import kotlinx.android.synthetic.main.statistics_detail_fragment.cardStatisticsDetailDates
import kotlinx.android.synthetic.main.statistics_detail_fragment.cardStatisticsDetailFilter
import kotlinx.android.synthetic.main.statistics_detail_fragment.cardStatisticsDetailRecords
import kotlinx.android.synthetic.main.statistics_detail_fragment.cardStatisticsDetailTotal
import kotlinx.android.synthetic.main.statistics_detail_fragment.chartStatisticsDetail
import kotlinx.android.synthetic.main.statistics_detail_fragment.chartStatisticsDetailSplit
import kotlinx.android.synthetic.main.statistics_detail_fragment.rvStatisticsDetailPreviewItems
import kotlinx.android.synthetic.main.statistics_detail_fragment.rvStatisticsDetailTagSplit
import kotlinx.android.synthetic.main.statistics_detail_fragment.spinnerStatisticsDetail
import kotlinx.android.synthetic.main.statistics_detail_fragment.viewStatisticsDetailItem
import javax.inject.Inject

@AndroidEntryPoint
class StatisticsDetailFragment :
    BaseFragment(),
    DateTimeDialogListener,
    TypesFilterDialogListener {

    override val layout: Int get() = R.layout.statistics_detail_fragment

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
            createStatisticsAdapterDelegate(onItemClick = { _, _ -> })
        )
    }
    private val params: StatisticsDetailParams by lazy {
        arguments?.getParcelable(ARGS_PARAMS) ?: StatisticsDetailParams()
    }

    override fun initUi() {
        setPreview()

        if (BuildVersions.isLollipopOrHigher()) {
            sharedElementEnterTransition = TransitionInflater.from(context)
                .inflateTransition(android.R.transition.move)
        }

        ViewCompat.setTransitionName(viewStatisticsDetailItem, params.transitionName)

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

    override fun initUx() {
        cardStatisticsDetailFilter.setOnClick(viewModel::onFilterClick)
        buttonsStatisticsDetailGrouping.listener = viewModel::onChartGroupingClick
        buttonsStatisticsDetailLength.listener = viewModel::onChartLengthClick
        buttonsStatisticsDetailSplitGrouping.listener = viewModel::onSplitChartGroupingClick
        cardStatisticsDetailRecords.listener = viewModel::onRecordsClick
        spinnerStatisticsDetail.onItemSelected = viewModel::onRangeClick
        btnStatisticsDetailPrevious.setOnClick(viewModel::onPreviousClick)
        btnStatisticsDetailNext.setOnClick(viewModel::onNextClick)
        btnStatisticsDetailToday.setOnClick { spinnerStatisticsDetail.performClick() }
        btnStatisticsDetailToday.setOnLongClick(viewModel::onTodayClick)
    }

    override fun onDateTimeSet(timestamp: Long, tag: String?) {
        viewModel.onDateTimeSet(timestamp, tag)
    }

    override fun initViewModel(): Unit = with(viewModel) {
        extra = params

        previewViewData.observe(::setPreviewViewData)
        statsViewData.observe(::setStatsViewData)
        chartViewData.observe(::updateChartViewData)
        splitChartViewData.observe(::updateSplitChartViewData)
        chartGroupingViewData.observe(::updateChartGroupingData)
        chartLengthViewData.observe(::updateChartLengthData)
        splitChartGroupingViewData.observe(::updateSplitChartGroupingData)
        title.observe(btnStatisticsDetailToday::setText)
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

    private fun setPreviewViewData(viewData: List<ViewHolderType>) {
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

    private fun setStatsViewData(statsViewData: StatisticsDetailStatsViewData) {
        cardStatisticsDetailTotal.items = statsViewData.totalDuration
        cardStatisticsDetailRecords.items = statsViewData.timesTracked
        cardStatisticsDetailAverage.items = statsViewData.averageRecord
        cardStatisticsDetailDates.items = statsViewData.datesTracked
        rvStatisticsDetailTagSplit.visible = statsViewData.tagSplitData.isNotEmpty()
        tagSplitAdapter.replace(statsViewData.tagSplitData)
    }

    private fun updateChartViewData(viewData: StatisticsDetailChartViewData) {
        chartStatisticsDetail.visible = viewData.visible
        chartStatisticsDetail.setBars(viewData.data)
        chartStatisticsDetail.setLegendTextSuffix(viewData.legendSuffix)
        chartStatisticsDetail.shouldAddLegendToSelectedBar(viewData.addLegendToSelectedBar)
        chartStatisticsDetail.shouldDrawHorizontalLegends(viewData.shouldDrawHorizontalLegends)
    }

    private fun updateChartGroupingData(viewData: List<ViewHolderType>) {
        buttonsStatisticsDetailGrouping.visible = viewData.isNotEmpty()
        buttonsStatisticsDetailGrouping.adapter.replace(viewData)
    }

    private fun updateSplitChartGroupingData(viewData: List<ViewHolderType>) {
        buttonsStatisticsDetailSplitGrouping.visible = viewData.isNotEmpty()
        buttonsStatisticsDetailSplitGrouping.adapter.replace(viewData)
    }

    private fun updateChartLengthData(viewData: List<ViewHolderType>) {
        buttonsStatisticsDetailLength.visible = viewData.isNotEmpty()
        buttonsStatisticsDetailLength.adapter.replace(viewData)
    }

    private fun updateSplitChartViewData(viewData: StatisticsDetailChartViewData) {
        chartStatisticsDetailSplit.visible = viewData.visible
        chartStatisticsDetailSplit.setBars(viewData.data)
        chartStatisticsDetailSplit.setLegendTextSuffix(viewData.legendSuffix)
        chartStatisticsDetailSplit.shouldAddLegendToSelectedBar(viewData.addLegendToSelectedBar)
        chartStatisticsDetailSplit.shouldDrawHorizontalLegends(viewData.shouldDrawHorizontalLegends)
    }

    private fun updateRangeItems(viewData: RangesViewData) {
        spinnerStatisticsDetail.setData(viewData.items, viewData.selectedPosition)
    }

    private fun updateRangeButtonsVisibility(isVisible: Boolean) {
        btnStatisticsDetailPrevious.visible = isVisible
        btnStatisticsDetailNext.visible = isVisible
    }

    companion object {
        private const val ARGS_PARAMS = "args_params"

        fun createBundle(data: Any?): Bundle = Bundle().apply {
            when (data) {
                is StatisticsDetailParams -> putParcelable(ARGS_PARAMS, data)
            }
        }
    }
}
