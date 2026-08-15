package com.example.util.simpletimetracker.feature_statistics_detail.viewModel.delegate

import androidx.lifecycle.LiveData
import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.core.extension.lazySuspend
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_statistics_detail.interactor.StatisticsDetailStatsInteractor
import com.example.util.simpletimetracker.feature_statistics_detail.mapper.mapItems
import com.example.util.simpletimetracker.feature_statistics_detail.mapper.mapToViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailViewData
import kotlinx.coroutines.launch
import javax.inject.Inject

class StatisticsDetailStatsViewModelDelegate @Inject constructor(
    private val statsInteractor: StatisticsDetailStatsInteractor,
) : StatisticsDetailViewModelDelegate, ViewModelDelegate() {

    val viewData: LiveData<List<ViewHolderType>> by lazySuspend {
        loadEmptyViewData().also { parent?.updateContent() }
    }

    private var parent: StatisticsDetailViewModelDelegate.Parent? = null

    override fun attach(parent: StatisticsDetailViewModelDelegate.Parent) {
        this.parent = parent
    }

    override fun getViewData(): StatisticsDetailViewData? {
        return viewData.value?.mapItems()?.let(::mapToViewData)
    }

    fun updateViewData() = delegateScope.launch {
        viewData.set(loadViewData())
        parent?.updateContent()
    }

    private fun loadEmptyViewData(): List<ViewHolderType> {
        return statsInteractor.getEmptyStatsViewData()
    }

    private suspend fun loadViewData(): List<ViewHolderType> {
        val parent = parent ?: return emptyList()

        return statsInteractor.getStatsViewData(
            records = parent.records,
            compareRecords = parent.compareRecords,
            showComparison = parent.comparisonFilter.isNotEmpty(),
            rangeLength = parent.rangeLength,
            rangePosition = parent.rangePosition,
        )
    }

    companion object : StatisticsDetailViewData.Key
}