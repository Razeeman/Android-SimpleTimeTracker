package com.example.util.simpletimetracker.feature_statistics_detail.interactor

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailPreviewCompositeViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewModel.delegate.StatisticsDetailChartViewModelDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.viewModel.delegate.StatisticsDetailDailyCalendarViewModelDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.viewModel.delegate.StatisticsDetailDataDistributionViewModelDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.viewModel.delegate.StatisticsDetailDurationSplitViewModelDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.viewModel.delegate.StatisticsDetailGoalsViewModelDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.viewModel.delegate.StatisticsDetailNextActivitiesViewModelDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.viewModel.delegate.StatisticsDetailPreviewViewModelDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.viewModel.delegate.StatisticsDetailSplitChartViewModelDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.viewModel.delegate.StatisticsDetailStatsViewModelDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.viewModel.delegate.StatisticsDetailStreaksViewModelDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.viewModel.delegate.StatisticsDetailTagValueViewModelDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.viewModel.delegate.StatisticsDetailViewModelDelegate
import javax.inject.Inject

class StatisticsDetailContentInteractor @Inject constructor() {

    fun getContent(
        previewViewData: StatisticsDetailPreviewCompositeViewData.Preview?,
        delegates: List<StatisticsDetailViewModelDelegate>,
    ): List<ViewHolderType> {
        val viewData = delegates
            .mapNotNull { it.getViewData() }
            .associateBy { it.key }

        return getBlocksOrder()
            .mapNotNull { key -> viewData[key]?.data }
            .flatten()
            .map { item -> item.itemProducer?.invoke(previewViewData) ?: item.item }
    }

    private fun getBlocksOrder(): List<StatisticsDetailViewData.Key> {
        return listOf(
            StatisticsDetailPreviewViewModelDelegate,
            StatisticsDetailChartViewModelDelegate,
            StatisticsDetailDailyCalendarViewModelDelegate,
            StatisticsDetailStatsViewModelDelegate,
            StatisticsDetailStreaksViewModelDelegate,
            StatisticsDetailSplitChartViewModelDelegate,
            StatisticsDetailDurationSplitViewModelDelegate,
            StatisticsDetailNextActivitiesViewModelDelegate,
            StatisticsDetailGoalsViewModelDelegate,
            StatisticsDetailTagValueViewModelDelegate,
            StatisticsDetailDataDistributionViewModelDelegate,
        )
    }
}