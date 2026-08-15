package com.example.util.simpletimetracker.feature_statistics_detail.viewModel.delegate

import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.record.model.RecordsFilter
import com.example.util.simpletimetracker.feature_statistics_detail.model.DataDistributionMode
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailViewData
import com.example.util.simpletimetracker.navigation.params.screen.StatisticsDetailParams

interface StatisticsDetailViewModelDelegate {

    fun attach(parent: Parent)
    fun getViewData(): StatisticsDetailViewData? = null

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