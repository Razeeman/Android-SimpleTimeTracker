package com.example.util.simpletimetracker.feature_statistics_detail.viewModel.delegate

import androidx.lifecycle.LiveData
import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.core.extension.lazySuspend
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_statistics_detail.interactor.StatisticsDetailPreviewInteractor
import com.example.util.simpletimetracker.feature_statistics_detail.interactor.StatisticsDetailTotalRecordsSelectedInteractor
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailPreview
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailPreviewCompositeViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailPreviewMoreViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailPreviewViewData
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.plus

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

    fun updateViewData() = delegateScope.launch {
        viewData.set(loadViewData())
        parent?.updateContent()
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
        val dateFilter = parent.getDateFilter()
        val total = totalRecordsSelectedInteractor.execute(currentFilter)

        val data = previewInteractor.getPreviewData(
            filterParams = currentFilter.takeIf { it.isNotEmpty() }
                ?.plus(dateFilter).orEmpty(),
            total = total,
            isExpanded = previewsExpanded,
            isForComparison = false,
        )
        val comparisonData = previewInteractor.getPreviewData(
            filterParams = parent.comparisonFilter.takeIf { it.isNotEmpty() }
                ?.plus(dateFilter).orEmpty(),
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

        return StatisticsDetailPreviewCompositeViewData(
            previewColor = previewColor,
            comparisonPreviewColor = comparisonPreviewColor,
            mainPreview = mainPreview,
            additionalData = additionalData,
            comparisonData = comparisonData,
        )
    }
}