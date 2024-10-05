package com.example.util.simpletimetracker.feature_statistics_detail.viewModel.delegate

import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.core.extension.toModel
import com.example.util.simpletimetracker.core.extension.toParams
import com.example.util.simpletimetracker.core.interactor.RecordFilterInteractor
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.model.RecordBase
import com.example.util.simpletimetracker.domain.model.RecordsFilter
import com.example.util.simpletimetracker.feature_statistics_detail.R
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.RecordsFilterParam
import com.example.util.simpletimetracker.navigation.params.screen.RecordsFilterParams
import com.example.util.simpletimetracker.navigation.params.screen.RecordsFilterResultParams
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class StatisticsDetailFilterViewModelDelegate @Inject constructor(
    private val router: Router,
    private val resourceRepo: ResourceRepo,
    private val recordFilterInteractor: RecordFilterInteractor,
    private val prefsInteractor: PrefsInteractor,
) : StatisticsDetailViewModelDelegate, ViewModelDelegate() {

    private var parent: StatisticsDetailViewModelDelegate.Parent? = null

    private val filter: MutableList<RecordsFilter> by lazy { loadFilter().toMutableList() }
    private val comparisonFilter: MutableList<RecordsFilter> = mutableListOf()
    private var records: List<RecordBase> = emptyList() // all records with selected ids
    private var compareRecords: List<RecordBase> = emptyList() // all records with selected ids
    private var loadJob: Job? = null
    private var firstLoad: Boolean = true

    override fun attach(parent: StatisticsDetailViewModelDelegate.Parent) {
        this.parent = parent
    }

    fun onVisible() {
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
        )
    }

    fun onCompareClick() = delegateScope.launch {
        openFilter(
            tag = COMPARE_TAG,
            title = resourceRepo.getString(R.string.types_compare_hint),
            filters = provideComparisonFilter(),
        )
    }

    fun onTypesFilterSelected(result: RecordsFilterResultParams) {
        // Remove date filter, because it is applied separately.
        val finalFilters = result.filters.filter { it !is RecordsFilter.Date }

        when (result.tag) {
            FILTER_TAG -> {
                filter.clear()
                filter.addAll(finalFilters)
            }
            COMPARE_TAG -> {
                comparisonFilter.clear()
                comparisonFilter.addAll(finalFilters)
            }
        }

        // Update is on dismiss.
    }

    fun onTypesFilterDismissed(tag: String) {
        if (tag !in listOf(FILTER_TAG, COMPARE_TAG)) return

        loadJob?.cancel()
        loadJob = delegateScope.launch {
            loadRecordsCache()
            parent?.onTypesFilterDismissed()
        }
    }

    fun provideRecords(): List<RecordBase> {
        return records
    }

    fun provideCompareRecords(): List<RecordBase> {
        return compareRecords
    }

    fun provideFilter(): List<RecordsFilter> {
        return filter.filter { it !is RecordsFilter.Date }
    }

    fun provideComparisonFilter(): List<RecordsFilter> {
        return comparisonFilter.filter { it !is RecordsFilter.Date }
    }

    private suspend fun openFilter(
        tag: String,
        title: String,
        filters: List<RecordsFilter>,
    ) {
        val parent = parent ?: return

        router.navigate(
            RecordsFilterParams(
                tag = tag,
                title = title,
                dateSelectionAvailable = false,
                untrackedSelectionAvailable = true,
                multitaskSelectionAvailable = true,
                addRunningRecords = true,
                filters = filters
                    .plus(parent.getDateFilter())
                    .map(RecordsFilter::toParams).toList(),
                defaultLastDaysNumber = prefsInteractor
                    .getStatisticsDetailLastDays(),
            ),
        )
    }

    // Delay data load until screen transition finishes
    // to avoid lagging while recycler is inflating views.
    // Only done when no shared transitions, they delay onResume.
    private suspend fun delayLoadIfNeeded() {
        val extra = parent?.extra ?: return
        if (extra.transitionName.isEmpty() && firstLoad) {
            delay(300)
            firstLoad = false
        }
    }

    private suspend fun loadRecordsCache() {
        // Load all records without date filter for faster date selection.
        records = recordFilterInteractor.getByFilter(filter)
        compareRecords = recordFilterInteractor.getByFilter(comparisonFilter)
    }

    private fun loadFilter(): List<RecordsFilter> {
        val parent = parent ?: return emptyList()

        return parent.extra.filter.map(RecordsFilterParam::toModel)
    }

    companion object {
        const val FILTER_TAG = "statistics_detail_filter_tag"
        const val COMPARE_TAG = "statistics_detail_compare_tag"
    }
}