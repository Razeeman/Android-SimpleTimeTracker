package com.example.util.simpletimetracker.feature_statistics_detail.viewModel.delegate

import androidx.lifecycle.LiveData
import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.core.extension.lazySuspend
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.domain.record.model.RecordsFilter
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailBlock
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailPreviewsViewData
import com.example.util.simpletimetracker.feature_statistics_detail.interactor.StatisticsDetailPreviewInteractor
import com.example.util.simpletimetracker.feature_statistics_detail.interactor.StatisticsDetailTotalRecordsSelectedInteractor
import com.example.util.simpletimetracker.feature_statistics_detail.mapper.mapToViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailPreview
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailPreviewCompositeViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailPreviewMoreViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailPreviewViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailViewData
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

class StatisticsDetailPreviewViewModelDelegate @Inject constructor(
    private val previewInteractor: StatisticsDetailPreviewInteractor,
    private val totalRecordsSelectedInteractor: StatisticsDetailTotalRecordsSelectedInteractor,
) : StatisticsDetailViewModelDelegate, ViewModelDelegate() {

    val viewData: LiveData<StatisticsDetailPreviewCompositeViewData?> by lazySuspend {
        loadViewData().also { parent?.updateContent() }
    }

    private var parent: StatisticsDetailViewModelDelegate.Parent? = null
    private var previewsExpanded: Boolean = false
    private var previewsComparisonExpanded: Boolean = false

    override fun attach(parent: StatisticsDetailViewModelDelegate.Parent) {
        this.parent = parent
    }

    override fun getViewData(): StatisticsDetailViewData? {
        return viewData.value?.data
    }

    override fun updateViewData(animate: Boolean) {
        delegateScope.launch {
            viewData.set(loadViewData())
            parent?.updateContent()
        }
    }

    fun onPreviewItemClick(item: StatisticsDetailPreview) {
        if (item !is StatisticsDetailPreviewMoreViewData) return
        when (item.type) {
            StatisticsDetailPreviewViewData.Type.FILTER -> previewsExpanded = true
            StatisticsDetailPreviewViewData.Type.COMPARISON -> previewsComparisonExpanded = true
        }
        updateViewData()
    }

    private suspend fun loadViewData(): StatisticsDetailPreviewCompositeViewData? {
        val parent = parent ?: return null
        val currentFilter = parent.filter
        val filtersWithoutDate = currentFilter.filter { it !is RecordsFilter.Date }
        val total = totalRecordsSelectedInteractor.execute(filtersWithoutDate)

        val data = previewInteractor.getPreviewData(
            filterParams = currentFilter,
            total = total,
            isExpanded = previewsExpanded,
            isForComparison = false,
        )
        val comparisonData = previewInteractor.getPreviewData(
            filterParams = parent.comparisonFilter,
            total = false,
            isExpanded = previewsComparisonExpanded,
            isForComparison = true,
        )
        val showMainPreview = total || data.size == 1

        val mainPreview: StatisticsDetailPreviewViewData?
        val additionalData: List<ViewHolderType>
        if (showMainPreview) {
            mainPreview = data.firstOrNull() as? StatisticsDetailPreviewViewData
            additionalData = data.drop(1)
        } else {
            mainPreview = null
            additionalData = data
        }

        val previewColor = mainPreview?.color
            ?: additionalData
                .filterIsInstance<StatisticsDetailPreviewViewData>()
                .firstOrNull { !it.isFiltered }
                ?.color

        val comparisonPreviewColor = comparisonData
            .filterIsInstance<StatisticsDetailPreviewViewData>()
            .firstOrNull { !it.isFiltered }
            ?.color

        val viewData = (additionalData + comparisonData)
            .takeIf { it.isNotEmpty() }
            ?.let {
                StatisticsDetailPreviewsViewData(
                    block = StatisticsDetailBlock.PreviewItems,
                    data = it,
                )
            }?.let {
                StatisticsDetailViewData.Item(it)
            }

        return StatisticsDetailPreviewCompositeViewData(
            data = mapToViewData(listOfNotNull(viewData)),
            preview = StatisticsDetailPreviewCompositeViewData.Preview(
                previewColor = previewColor,
                comparisonPreviewColor = comparisonPreviewColor,
                mainPreview = mainPreview,
            ),
        )
    }

    companion object : StatisticsDetailViewData.Key
}