package com.example.util.simpletimetracker.feature_statistics_detail.viewModel.delegate

import com.example.util.simpletimetracker.domain.base.Coordinates
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.record.model.RecordsFilter
import com.example.util.simpletimetracker.domain.statistics.model.StatisticsDetailTagValueSettings
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.buttonsRow.ButtonsRowItemViewData
import com.example.util.simpletimetracker.feature_base_adapter.buttonsRow.view.ButtonsRowViewData
import com.example.util.simpletimetracker.feature_base_adapter.statistics.StatisticsViewData
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailBlock
import com.example.util.simpletimetracker.feature_statistics_detail.customView.SeriesCalendarView
import com.example.util.simpletimetracker.feature_statistics_detail.model.DataDistributionMode
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailPreview
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailViewData
import com.example.util.simpletimetracker.navigation.params.screen.RecordsFilterResultParams
import com.example.util.simpletimetracker.navigation.params.screen.StatisticsDetailParams

interface StatisticsDetailViewModelDelegate {

    fun attach(parent: Parent)
    fun getViewData(): StatisticsDetailViewData? = null
    fun updateViewData(animate: Boolean = true) = Unit
    fun initialize(extra: StatisticsDetailParams) = Unit
    fun onVisible() = Unit
    fun onTypesFilterSelected(result: RecordsFilterResultParams) = Unit
    fun onTypesFilterDismissed(tag: String) = Unit
    suspend fun doOnFiltersChanged() = Unit
    fun onButtonsRowClick(block: ButtonsRowItemViewData.ButtonsRowId, viewData: ButtonsRowViewData) = Unit
    fun onButtonClick(block: StatisticsDetailBlock) = Unit
    fun onChartClick(block: StatisticsDetailBlock, barId: Long?) = Unit
    fun onPreviewItemClick(item: StatisticsDetailPreview) = Unit
    fun onPreviewItemLongClick(item: StatisticsDetailPreview) = Unit
    fun onStatisticsItemClick(item: StatisticsViewData) = Unit
    fun onSwipedStart(item: ViewHolderType) = Unit
    fun onSwipedEnd(item: ViewHolderType) = Unit
    fun onTagValuesSettingsChanged(result: StatisticsDetailTagValueSettings) = Unit
    fun onStreaksCalendarClick(viewData: SeriesCalendarView.ViewData, coordinates: Coordinates) = Unit

    interface Parent {
        val extra: StatisticsDetailParams
        val records: List<RecordBase>
        val compareRecords: List<RecordBase>
        val filter: List<RecordsFilter>
        val comparisonFilter: List<RecordsFilter>
        val rangeLength: RangeLength
        val rangePosition: Int

        fun updateContent()
        suspend fun onRangeChangedFromSelection(newRange: RangeLength)
        fun onPositionChangedFromSelection(newPosition: Int)
        fun updateViewData()
        suspend fun onFiltersChanged()
        fun onStatisticsHidden(id: Long, mode: DataDistributionMode)
        fun onStatisticsOtherHidden(id: Long, mode: DataDistributionMode)
    }
}