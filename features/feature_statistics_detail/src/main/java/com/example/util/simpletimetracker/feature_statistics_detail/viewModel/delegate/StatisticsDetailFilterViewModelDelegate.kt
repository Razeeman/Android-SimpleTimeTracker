package com.example.util.simpletimetracker.feature_statistics_detail.viewModel.delegate

import com.example.util.simpletimetracker.core.base.DelayLoadHandler
import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.core.extension.toModel
import com.example.util.simpletimetracker.core.extension.toParams
import com.example.util.simpletimetracker.core.interactor.RecordFilterInteractor
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.extension.removeIf
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.record.model.RecordsFilter
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import com.example.util.simpletimetracker.feature_records_filter.api.RecordsFilterExcludeInteractor
import com.example.util.simpletimetracker.feature_statistics_detail.R
import com.example.util.simpletimetracker.feature_statistics_detail.model.DataDistributionMode
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailPreview
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailPreviewViewData
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.RecordsFilterParam
import com.example.util.simpletimetracker.navigation.params.screen.RecordsFilterParams
import com.example.util.simpletimetracker.navigation.params.screen.RecordsFilterResultParams
import com.example.util.simpletimetracker.navigation.params.screen.StatisticsDetailParams
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

class StatisticsDetailFilterViewModelDelegate @Inject constructor(
    private val router: Router,
    private val resourceRepo: ResourceRepo,
    private val recordFilterInteractor: RecordFilterInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val recordsFilterExcludeInteractor: RecordsFilterExcludeInteractor,
) : StatisticsDetailViewModelDelegate, ViewModelDelegate(), DelayLoadHandler {

    override var delayDataLoad: Boolean = true

    private var parent: StatisticsDetailViewModelDelegate.Parent? = null

    private var filter: List<RecordsFilter> = emptyList()
    private var comparisonFilter: List<RecordsFilter> = mutableListOf()
    private var records: List<RecordBase> = emptyList() // all records with selected ids
    private var compareRecords: List<RecordBase> = emptyList() // all records with selected ids
    private var loadJob: Job? = null

    override fun attach(parent: StatisticsDetailViewModelDelegate.Parent) {
        this.parent = parent
    }

    override fun initialize(extra: StatisticsDetailParams) {
        filter = loadFilter(extra).toMutableList()
    }

    override fun onVisible() {
        loadJob?.cancel()
        loadJob = delegateScope.launch {
            delayLoadIfNeeded()
            loadRecordsCache()
            parent?.updateViewData()
        }
    }

    fun onFilterClick() = delegateScope.launch {
        openFilter(
            tag = FILTER_TAG,
            title = resourceRepo.getString(R.string.chart_filter_hint),
            filters = provideFilter(),
            dateSelectionAvailable = true,
        )
    }

    fun onCompareClick() = delegateScope.launch {
        openFilter(
            tag = COMPARE_TAG,
            title = resourceRepo.getString(R.string.types_compare_hint),
            filters = provideComparisonFilter(),
            dateSelectionAvailable = false,
        )
    }

    override fun onTypesFilterSelected(result: RecordsFilterResultParams) {
        val finalFilters = result.filters

        when (result.tag) {
            // Reapply default date if date filter was removed.
            FILTER_TAG -> {
                filter = if (finalFilters.none { it is RecordsFilter.Date }) {
                    finalFilters + getDefaultDateFilter()
                } else {
                    finalFilters
                }
            }
            // Remove date filter, because it is taken from main filters.
            COMPARE_TAG -> {
                comparisonFilter = finalFilters.filter { it !is RecordsFilter.Date }
            }
        }
        if (result.tag == FILTER_TAG) updateSelectedRange()

        // Update is on dismiss.
    }

    override fun onTypesFilterDismissed(tag: String) {
        if (tag !in listOf(FILTER_TAG, COMPARE_TAG)) return
        onFiltersChanged()
    }

    fun onStatisticsHidden(id: Long, mode: DataDistributionMode) = delegateScope.launch {
        filter = recordsFilterExcludeInteractor.exclude(
            id = id,
            type = mapExcludeType(mode),
            currentFilters = filter,
        )
        onFiltersChanged()
    }

    fun onStatisticsOtherHidden(id: Long, mode: DataDistributionMode) = delegateScope.launch {
        filter = recordsFilterExcludeInteractor.excludeOther(
            id = id,
            type = mapExcludeType(mode),
            currentFilters = filter,
        )
        onFiltersChanged()
    }

    override fun onPreviewItemClick(item: StatisticsDetailPreview) {
        if (item !is StatisticsDetailPreviewViewData) return
        delegateScope.launch {
            val newFilter = recordsFilterExcludeInteractor.exclude(
                id = item.id,
                type = mapPreviewDataTypeToExcludeType(item.dataType) ?: return@launch,
                currentFilters = when (item.type) {
                    StatisticsDetailPreviewViewData.Type.FILTER -> filter
                    StatisticsDetailPreviewViewData.Type.COMPARISON -> comparisonFilter
                },
            )
            when (item.type) {
                StatisticsDetailPreviewViewData.Type.FILTER -> filter = newFilter
                StatisticsDetailPreviewViewData.Type.COMPARISON -> comparisonFilter = newFilter
            }
            onFiltersChanged()
        }
    }

    override fun onPreviewItemLongClick(item: StatisticsDetailPreview) {
        if (item !is StatisticsDetailPreviewViewData) return
        delegateScope.launch {
            val newFilter = recordsFilterExcludeInteractor.excludeOther(
                id = item.id,
                type = mapPreviewDataTypeToExcludeType(item.dataType) ?: return@launch,
                currentFilters = when (item.type) {
                    StatisticsDetailPreviewViewData.Type.FILTER -> filter
                    StatisticsDetailPreviewViewData.Type.COMPARISON -> comparisonFilter
                },
            )
            when (item.type) {
                StatisticsDetailPreviewViewData.Type.FILTER -> filter = newFilter
                StatisticsDetailPreviewViewData.Type.COMPARISON -> comparisonFilter = newFilter
            }
            onFiltersChanged()
        }
    }

    fun onRangeChangedFromSelection(newRange: RangeLength) {
        val newDate = RecordsFilter.Date(
            range = newRange,
            position = getCurrentDateFilter().position,
        )
        filter = filter.removeIf { it is RecordsFilter.Date } + newDate
        // Do not reload cache on date change.
        onFiltersChanged(reloadCache = false)
    }

    fun onPositionChangedFromSelection(newPosition: Int) {
        val newDate = RecordsFilter.Date(
            range = getCurrentDateFilter().range,
            position = newPosition,
        )
        filter = filter.removeIf { it is RecordsFilter.Date } + newDate
        // Do not reload cache on date change.
        onFiltersChanged(reloadCache = false)
    }

    fun provideRecords(): List<RecordBase> {
        return records
    }

    fun provideCompareRecords(): List<RecordBase> {
        return compareRecords
    }

    fun provideFilter(): List<RecordsFilter> {
        return filter
    }

    fun provideComparisonFilter(): List<RecordsFilter> {
        return comparisonFilter
            .takeIf { it.isNotEmpty() }
            ?.plus(getCurrentDateFilter()).orEmpty()
    }

    private fun updateSelectedRange() {
        delegateScope.launch {
            prefsInteractor.setStatisticsDetailRange(getCurrentDateFilter().range)
        }
    }

    private fun getCurrentDateFilter(): RecordsFilter.Date {
        return filter.filterIsInstance<RecordsFilter.Date>().firstOrNull()
            ?: getDefaultDateFilter()
    }

    private fun getDefaultDateFilter(): RecordsFilter.Date {
        return RecordsFilter.Date(RangeLength.All, 0)
    }

    private fun onFiltersChanged(reloadCache: Boolean = true) {
        loadJob?.cancel()
        loadJob = delegateScope.launch {
            if (reloadCache) loadRecordsCache()
            parent?.onFiltersChanged()
        }
    }

    private suspend fun openFilter(
        tag: String,
        title: String,
        filters: List<RecordsFilter>,
        dateSelectionAvailable: Boolean,
    ) {
        router.navigate(
            RecordsFilterParams(
                tag = tag,
                title = title,
                flags = RecordsFilterParams.Flags(
                    dateSelectionAvailable = dateSelectionAvailable,
                    untrackedSelectionAvailable = true,
                    multitaskSelectionAvailable = true,
                    duplicationsSelectionAvailable = false,
                    favouriteSelectionAvailable = true,
                    addRunningRecords = true,
                ),
                filters = filters
                    .map(RecordsFilter::toParams).toList(),
                defaultLastDaysNumber = prefsInteractor
                    .getStatisticsDetailLastDays(),
            ),
        )
    }

    private fun mapExcludeType(mode: DataDistributionMode): RecordsFilterExcludeInteractor.ExcludeType {
        val type = when (mode) {
            DataDistributionMode.ACTIVITY -> RecordsFilterExcludeInteractor.ExcludeType.Activity
            DataDistributionMode.CATEGORY -> RecordsFilterExcludeInteractor.ExcludeType.Category
            DataDistributionMode.TAG -> RecordsFilterExcludeInteractor.ExcludeType.Tag
        }
        return type
    }

    // Delay data load until screen transition finishes
    // to avoid lagging while recycler is inflating views.
    // Only done when no shared transitions, they delay onResume.
    private suspend fun delayLoadIfNeeded() {
        val extra = parent?.extra ?: return
        if (extra.transitionName.isEmpty()) {
            delayLoad()
        }
    }

    private fun mapPreviewDataTypeToExcludeType(
        data: StatisticsDetailPreviewViewData.DataType,
    ): RecordsFilterExcludeInteractor.ExcludeType? {
        return when (data) {
            StatisticsDetailPreviewViewData.DataType.ACTIVITY ->
                RecordsFilterExcludeInteractor.ExcludeType.Activity
            StatisticsDetailPreviewViewData.DataType.CATEGORY ->
                RecordsFilterExcludeInteractor.ExcludeType.Category
            StatisticsDetailPreviewViewData.DataType.TAG ->
                RecordsFilterExcludeInteractor.ExcludeType.Tag
            StatisticsDetailPreviewViewData.DataType.OTHER ->
                return null
        }
    }

    private suspend fun loadRecordsCache() {
        // Load all records without date filter for faster date selection.
        val filtersWithoutDate = filter
            .filter { it !is RecordsFilter.Date }
            .ifEmpty { listOf(getDefaultDateFilter()) }
        val comparisonFiltersWithoutDate = comparisonFilter
            .filter { it !is RecordsFilter.Date }
        records = recordFilterInteractor.getByFilter(filtersWithoutDate)
        compareRecords = recordFilterInteractor.getByFilter(comparisonFiltersWithoutDate)
    }

    private fun loadFilter(extra: StatisticsDetailParams): List<RecordsFilter> {
        val date = RecordsFilter.Date(
            range = extra.range.toModel(),
            position = extra.shift,
        )
        return extra.filter.map(RecordsFilterParam::toModel) + date
    }

    companion object {
        const val FILTER_TAG = "statistics_detail_filter_tag"
        const val COMPARE_TAG = "statistics_detail_compare_tag"
    }
}