package com.example.util.simpletimetracker.feature_statistics_detail.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionInflater
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.hint.createHintAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.statistics.createStatisticsAdapterDelegate
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.dialog.DateTimeDialogListener
import com.example.util.simpletimetracker.core.dialog.TypesFilterDialogListener
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_views.extension.setOnLongClick
import com.example.util.simpletimetracker.core.extension.toViewData
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.core.utils.BuildVersions
import com.example.util.simpletimetracker.core.viewData.RangesViewData
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
import javax.inject.Inject
import com.example.util.simpletimetracker.feature_statistics_detail.databinding.StatisticsDetailFragmentBinding as Binding

@AndroidEntryPoint
class StatisticsDetailFragment :
    BaseFragment<Binding>(),
    DateTimeDialogListener,
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
            createStatisticsAdapterDelegate(onItemClick = { _, _ -> })
        )
    }
    private val params: StatisticsDetailParams by lazy {
        arguments?.getParcelable(ARGS_PARAMS) ?: StatisticsDetailParams()
    }

    override fun initUi(): Unit = with(binding) {
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

    override fun initUx() = with(binding) {
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

    private fun updateChartViewData(viewData: StatisticsDetailChartViewData) = with(binding) {
        chartStatisticsDetail.visible = viewData.visible
        chartStatisticsDetail.setBars(viewData.data)
        chartStatisticsDetail.setLegendTextSuffix(viewData.legendSuffix)
        chartStatisticsDetail.shouldAddLegendToSelectedBar(viewData.addLegendToSelectedBar)
        chartStatisticsDetail.shouldDrawHorizontalLegends(viewData.shouldDrawHorizontalLegends)
    }

    private fun updateChartGroupingData(viewData: List<ViewHolderType>) = with(binding) {
        buttonsStatisticsDetailGrouping.visible = viewData.isNotEmpty()
        buttonsStatisticsDetailGrouping.adapter.replace(viewData)
    }

    private fun updateSplitChartGroupingData(viewData: List<ViewHolderType>) = with(binding) {
        buttonsStatisticsDetailSplitGrouping.visible = viewData.isNotEmpty()
        buttonsStatisticsDetailSplitGrouping.adapter.replace(viewData)
    }

    private fun updateChartLengthData(viewData: List<ViewHolderType>) = with(binding) {
        buttonsStatisticsDetailLength.visible = viewData.isNotEmpty()
        buttonsStatisticsDetailLength.adapter.replace(viewData)
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

        fun createBundle(data: Any?): Bundle = Bundle().apply {
            when (data) {
                is StatisticsDetailParams -> putParcelable(ARGS_PARAMS, data)
            }
        }
    }
}
