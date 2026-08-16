package com.example.util.simpletimetracker.feature_statistics_detail.viewModel.delegate

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.feature_base_adapter.buttonsRow.view.ButtonsRowViewData
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.buttonsRow.ButtonsRowItemViewData
import com.example.util.simpletimetracker.feature_base_adapter.statistics.StatisticsSelectableViewData
import com.example.util.simpletimetracker.feature_base_adapter.statistics.StatisticsViewData
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailBlock
import com.example.util.simpletimetracker.feature_statistics_detail.interactor.StatisticsDetailDataDistributionInteractor
import com.example.util.simpletimetracker.feature_statistics_detail.mapper.mapItems
import com.example.util.simpletimetracker.feature_statistics_detail.mapper.mapToViewData
import com.example.util.simpletimetracker.feature_statistics_detail.model.DataDistributionMode
import com.example.util.simpletimetracker.feature_statistics_detail.model.DataDistributionGraph
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailDataDistributionModeViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailDataDistributionGraphViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailDataDistributionViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailViewData
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

class StatisticsDetailDataDistributionViewModelDelegate @Inject constructor(
    private val dataDistributionInteractor: StatisticsDetailDataDistributionInteractor,
) : StatisticsDetailViewModelDelegate, ViewModelDelegate() {

    val viewData: LiveData<StatisticsDetailDataDistributionViewData?> by lazy {
        MutableLiveData()
    }

    private var parent: StatisticsDetailViewModelDelegate.Parent? = null
    private var updateViewDataJob: Job? = null
    private var dataDistributionMode = DataDistributionMode.ACTIVITY
    private var dataDistributionGraph = DataDistributionGraph.PIE_CHART
    private var selectedItemId: Long? = null

    override fun attach(parent: StatisticsDetailViewModelDelegate.Parent) {
        this.parent = parent
    }

    override fun getViewData(): StatisticsDetailViewData? {
        return viewData.value?.splitData?.mapItems()?.let(::mapToViewData)
    }

    override fun onButtonsRowClick(
        block: ButtonsRowItemViewData.ButtonsRowId,
        viewData: ButtonsRowViewData,
    ) {
        when (block) {
            StatisticsDetailBlock.DataDistributionMode -> onDataDistributionModeClick(viewData)
            StatisticsDetailBlock.DataDistributionGraph -> onDataDistributionGraphClick(viewData)
        }
    }

    override fun onChartClick(
        block: StatisticsDetailBlock,
        barId: Long?,
    ) {
        when (block) {
            StatisticsDetailBlock.DataDistributionBarChart -> onChartClick(barId)
            StatisticsDetailBlock.DataDistributionPieChart -> onChartClick(barId)
            else -> Unit
        }
    }

    private fun onDataDistributionModeClick(viewData: ButtonsRowViewData) {
        if (viewData !is StatisticsDetailDataDistributionModeViewData) return
        this.dataDistributionMode = viewData.mode
        selectedItemId = null
        updateViewData()
    }

    private fun onDataDistributionGraphClick(viewData: ButtonsRowViewData) {
        if (viewData !is StatisticsDetailDataDistributionGraphViewData) return
        this.dataDistributionGraph = viewData.graph
        updateViewData()
    }

    override fun onStatisticsItemClick(item: StatisticsViewData) {
        selectedItemId = if (selectedItemId == item.id) null else item.id
        updateViewData(animate = false)
    }

    private fun onChartClick(barId: Long?) {
        selectedItemId = barId
        updateViewData(animate = false)
    }

    override fun onSwipedStart(item: ViewHolderType) {
        val id = (item as? StatisticsSelectableViewData)?.data?.id ?: return
        parent?.onStatisticsHidden(
            id = id,
            mode = dataDistributionMode,
        )
    }

    override fun onSwipedEnd(item: ViewHolderType) {
        val id = (item as? StatisticsSelectableViewData)?.data?.id ?: return
        parent?.onStatisticsOtherHidden(
            id = id,
            mode = dataDistributionMode,
        )
    }

    override fun updateViewData(animate: Boolean) {
        updateViewDataJob?.cancel()
        updateViewDataJob = delegateScope.launch {
            viewData.set(loadViewData(animate))
            parent?.updateContent()
        }
    }

    private suspend fun loadViewData(
        animate: Boolean,
    ): StatisticsDetailDataDistributionViewData? {
        val parent = parent ?: return null

        return dataDistributionInteractor.getViewData(
            records = parent.records,
            rangeLength = parent.rangeLength,
            rangePosition = parent.rangePosition,
            dataDistributionMode = dataDistributionMode,
            dataDistributionGraph = dataDistributionGraph,
            selectedItemId = selectedItemId,
            animate = animate,
        )
    }

    companion object : StatisticsDetailViewData.Key
}