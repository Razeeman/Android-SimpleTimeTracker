package com.example.util.simpletimetracker.feature_statistics_detail.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.BaseViewModel
import com.example.util.simpletimetracker.core.base.SingleLiveEvent
import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.core.extension.lazySuspend
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.extension.toParams
import com.example.util.simpletimetracker.core.viewData.RangeSelectionOptionsListItem
import com.example.util.simpletimetracker.domain.base.Coordinates
import com.example.util.simpletimetracker.domain.record.model.Range
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.record.model.RecordsFilter
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import com.example.util.simpletimetracker.domain.statistics.model.StatisticsDetailTagValueSettings
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.buttonsRow.ButtonsRowItemViewData
import com.example.util.simpletimetracker.feature_base_adapter.buttonsRow.view.ButtonsRowViewData
import com.example.util.simpletimetracker.feature_base_adapter.statistics.StatisticsViewData
import com.example.util.simpletimetracker.feature_date_selection.api.DateSelectorMapper
import com.example.util.simpletimetracker.feature_date_selection.api.DateSelectorViewModelDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailBlock
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailPreviewsViewData
import com.example.util.simpletimetracker.feature_statistics_detail.api.StatisticsDetailOptionsListItem
import com.example.util.simpletimetracker.feature_statistics_detail.api.StatisticsDetailOptionsListMapper
import com.example.util.simpletimetracker.feature_statistics_detail.customView.SeriesCalendarView
import com.example.util.simpletimetracker.feature_statistics_detail.interactor.StatisticsDetailContentInteractor
import com.example.util.simpletimetracker.feature_statistics_detail.model.DataDistributionMode
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailCardInternalViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailClickablePopup
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailClickableTracked
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailPreview
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailPreviewCompositeViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewModel.delegate.StatisticsDetailChartViewModelDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.viewModel.delegate.StatisticsDetailDailyCalendarViewModelDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.viewModel.delegate.StatisticsDetailDataDistributionViewModelDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.viewModel.delegate.StatisticsDetailDurationSplitViewModelDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.viewModel.delegate.StatisticsDetailFilterViewModelDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.viewModel.delegate.StatisticsDetailGoalsViewModelDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.viewModel.delegate.StatisticsDetailNextActivitiesViewModelDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.viewModel.delegate.StatisticsDetailPreviewViewModelDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.viewModel.delegate.StatisticsDetailRangeViewModelDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.viewModel.delegate.StatisticsDetailSplitChartViewModelDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.viewModel.delegate.StatisticsDetailStatsViewModelDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.viewModel.delegate.StatisticsDetailStreaksViewModelDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.viewModel.delegate.StatisticsDetailTagValueViewModelDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.viewModel.delegate.StatisticsDetailViewModelDelegate
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.notification.PopupParams
import com.example.util.simpletimetracker.navigation.params.screen.OptionsListParams
import com.example.util.simpletimetracker.navigation.params.screen.RecordsAllParams
import com.example.util.simpletimetracker.navigation.params.screen.RecordsFilterResultParams
import com.example.util.simpletimetracker.navigation.params.screen.StatisticsDetailParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatisticsDetailViewModel @Inject constructor(
    val dateSelectorViewModelDelegate: DateSelectorViewModelDelegate,
    private val router: Router,
    private val statisticsDetailContentInteractor: StatisticsDetailContentInteractor,
    private val previewDelegate: StatisticsDetailPreviewViewModelDelegate,
    statsDelegate: StatisticsDetailStatsViewModelDelegate,
    streaksDelegate: StatisticsDetailStreaksViewModelDelegate,
    chartDelegate: StatisticsDetailChartViewModelDelegate,
    splitChartDelegate: StatisticsDetailSplitChartViewModelDelegate,
    nextActivitiesDelegate: StatisticsDetailNextActivitiesViewModelDelegate,
    durationSplitDelegate: StatisticsDetailDurationSplitViewModelDelegate,
    private val rangeDelegate: StatisticsDetailRangeViewModelDelegate,
    private val filterDelegate: StatisticsDetailFilterViewModelDelegate,
    dailyCalendarDelegate: StatisticsDetailDailyCalendarViewModelDelegate,
    goalsDelegate: StatisticsDetailGoalsViewModelDelegate,
    dataDistributionDelegate: StatisticsDetailDataDistributionViewModelDelegate,
    tagValueDelegate: StatisticsDetailTagValueViewModelDelegate,
    private val statisticsDetailOptionsListMapper: StatisticsDetailOptionsListMapper,
) : BaseViewModel() {

    val scrollToTop: LiveData<Unit> = SingleLiveEvent()
    val content: LiveData<List<ViewHolderType>> by lazySuspend { loadContent() }
    val previewViewData: LiveData<StatisticsDetailPreviewCompositeViewData?> by previewDelegate::viewData

    private lateinit var extra: StatisticsDetailParams
    private var scrolledToTop: Boolean = false

    private val delegates: List<StatisticsDetailViewModelDelegate> = listOf(
        previewDelegate,
        statsDelegate,
        streaksDelegate,
        chartDelegate,
        splitChartDelegate,
        nextActivitiesDelegate,
        durationSplitDelegate,
        rangeDelegate,
        filterDelegate,
        dailyCalendarDelegate,
        goalsDelegate,
        dataDistributionDelegate,
        tagValueDelegate,
    )

    init {
        val delegateParent = getDelegateParent()
        delegates.forEach { it.attach(delegateParent) }
        dateSelectorViewModelDelegate.attach(getDateSelectorDelegateParent())
    }

    override fun onCleared() {
        delegates.forEach { (it as? ViewModelDelegate)?.clear() }
        super.onCleared()
    }

    fun initialize(extra: StatisticsDetailParams) {
        if (this::extra.isInitialized) return
        this.extra = extra
        delegates.forEach { it.initialize(extra) }
        viewModelScope.launch {
            dateSelectorViewModelDelegate.initialize(rangeDelegate.provideRangePosition())
        }
    }

    fun onVisible() {
        delegates.forEach { it.onVisible() }
        // TODO update only when necessary?
        viewModelScope.launch {
            dateSelectorViewModelDelegate.setup()
            dateSelectorViewModelDelegate.updatePosition(rangeDelegate.provideRangePosition())
        }
    }

    fun onTypesFilterSelected(result: RecordsFilterResultParams) {
        delegates.forEach { it.onTypesFilterSelected(result) }
    }

    fun onTypesFilterDismissed(tag: String) {
        delegates.forEach { it.onTypesFilterDismissed(tag) }
    }

    fun onButtonsRowClick(
        block: ButtonsRowItemViewData.ButtonsRowId,
        viewData: ButtonsRowViewData,
    ) {
        delegates.forEach { it.onButtonsRowClick(block, viewData) }
    }

    fun onButtonClick(block: StatisticsDetailBlock) {
        delegates.forEach { it.onButtonClick(block) }
    }

    fun onCardClick(
        type: StatisticsDetailCardInternalViewData.ClickableType,
        coordinates: Coordinates,
    ) {
        when (type) {
            is StatisticsDetailClickableTracked -> {
                onRecordsClick()
            }
            is StatisticsDetailClickablePopup -> {
                PopupParams(
                    message = type.message,
                    anchorCoordinates = coordinates,
                ).let(router::show)
            }
        }
    }

    fun onStatisticsItemClick(
        item: StatisticsViewData,
        @Suppress("UNUSED_PARAMETER") sharedElements: Map<Any, String>,
    ) {
        delegates.forEach { it.onStatisticsItemClick(item) }
    }

    fun onPreviewItemClick(item: StatisticsDetailPreview) {
        delegates.forEach { it.onPreviewItemClick(item) }
    }

    fun onPreviewItemLongClick(item: StatisticsDetailPreview) {
        delegates.forEach { it.onPreviewItemLongClick(item) }
    }

    fun onChartClick(block: StatisticsDetailBlock, barId: Long?) {
        delegates.forEach { it.onChartClick(block, barId) }
    }

    fun onSwipedStart(item: ViewHolderType?) {
        item ?: return
        delegates.forEach { it.onSwipedStart(item) }
    }

    fun onSwipedEnd(item: ViewHolderType?) {
        item ?: return
        delegates.forEach { it.onSwipedEnd(item) }
    }

    fun onOptionsClick() = viewModelScope.launch {
        val items = statisticsDetailOptionsListMapper.map(
            filterHidden = true,
            rangeLength = rangeDelegate.provideRangeLength(),
        )
        when {
            items.isEmpty() -> return@launch
            items.size == 1 -> items.firstOrNull()?.id?.let(::onOptionsItemClick)
            else -> router.navigate(OptionsListParams(items))
        }
    }

    fun onOptionsLongClick() {
        filterDelegate.onFilterClick()
    }

    fun onOptionsItemClick(id: OptionsListParams.Item.Id) {
        if (id !is StatisticsDetailOptionsListItem) return
        when (id) {
            is StatisticsDetailOptionsListItem.Filter -> filterDelegate.onFilterClick()
            is StatisticsDetailOptionsListItem.Compare -> filterDelegate.onCompareClick()
            is StatisticsDetailOptionsListItem.BackToToday -> rangeDelegate.onBackToTodayClick()
            is StatisticsDetailOptionsListItem.SelectDate -> rangeDelegate.onSelectDateClick()
            is StatisticsDetailOptionsListItem.SelectRange -> rangeDelegate.onSelectRangeClick()
        }
    }

    fun onRangeSelected(id: RangeSelectionOptionsListItem) {
        rangeDelegate.onRangeSelected(id)
    }

    fun onDateTimeSet(timestamp: Long, tag: String?) {
        rangeDelegate.onDateTimeSet(timestamp, tag)
    }

    fun onCustomRangeSelected(range: Range) {
        rangeDelegate.onCustomRangeSelected(range)
    }

    fun onCountSet(count: Long, tag: String?) = viewModelScope.launch {
        rangeDelegate.onCountSet(count, tag)
    }

    fun onTagValuesSettingsChanged(result: StatisticsDetailTagValueSettings) {
        delegates.forEach { it.onTagValuesSettingsChanged(result) }
    }

    fun onStreaksCalendarClick(viewData: SeriesCalendarView.ViewData, coordinates: Coordinates) {
        delegates.forEach { it.onStreaksCalendarClick(viewData, coordinates) }
    }

    fun onBackPressed() {
        router.back()
    }

    private fun onRecordsClick() {
        val finalFilters = filterDelegate.provideFilter()
            .map(RecordsFilter::toParams).toList()

        router.navigate(RecordsAllParams(finalFilters))
    }

    private fun checkTopScroll(
        newData: List<ViewHolderType>,
    ) {
        val previewsWillBeShown = newData.any { it is StatisticsDetailPreviewsViewData }
        if (previewsWillBeShown && !scrolledToTop) {
            scrolledToTop = true
            scrollToTop.set(Unit)
        }
    }

    private fun updateViewData() {
        delegates.forEach { it.updateViewData() }
        dateSelectorViewModelDelegate.updatePosition(rangeDelegate.provideRangePosition())
    }

    private fun updateContent() {
        val data = loadContent()
        content.set(data)
        checkTopScroll(data)
    }

    // TODO remove liveData, access simple field instead
    private fun loadContent(): List<ViewHolderType> {
        return statisticsDetailContentInteractor.getContent(
            previewViewData = previewViewData.value?.preview,
            delegates = delegates,
        )
    }

    private fun getDelegateParent(): StatisticsDetailViewModelDelegate.Parent {
        return object : StatisticsDetailViewModelDelegate.Parent {
            override val extra: StatisticsDetailParams
                get() = this@StatisticsDetailViewModel.extra
            override val records: List<RecordBase>
                get() = this@StatisticsDetailViewModel.filterDelegate.provideRecords()
            override val compareRecords: List<RecordBase>
                get() = this@StatisticsDetailViewModel.filterDelegate.provideCompareRecords()
            override val filter: List<RecordsFilter>
                get() = this@StatisticsDetailViewModel.filterDelegate.provideFilter()
            override val comparisonFilter: List<RecordsFilter>
                get() = this@StatisticsDetailViewModel.filterDelegate.provideComparisonFilter()
            override val rangeLength: RangeLength
                get() = this@StatisticsDetailViewModel.rangeDelegate.provideRangeLength()
            override val rangePosition: Int
                get() = this@StatisticsDetailViewModel.rangeDelegate.provideRangePosition()

            override fun updateContent() {
                this@StatisticsDetailViewModel.updateContent()
            }

            override suspend fun onRangeChangedFromSelection(newRange: RangeLength) {
                filterDelegate.onRangeChangedFromSelection(newRange)
            }

            override fun onPositionChangedFromSelection(newPosition: Int) {
                filterDelegate.onPositionChangedFromSelection(newPosition)
            }

            override fun updateViewData() {
                this@StatisticsDetailViewModel.updateViewData()
            }

            override suspend fun onFiltersChanged() {
                dateSelectorViewModelDelegate.setup()
                delegates.forEach { it.doOnFiltersChanged() }
                this@StatisticsDetailViewModel.updateViewData()
            }

            override fun onStatisticsHidden(id: Long, mode: DataDistributionMode) {
                filterDelegate.onStatisticsHidden(id, mode)
            }

            override fun onStatisticsOtherHidden(id: Long, mode: DataDistributionMode) {
                filterDelegate.onStatisticsOtherHidden(id, mode)
            }
        }
    }

    private fun getDateSelectorDelegateParent(): DateSelectorViewModelDelegate.Parent {
        return object : DateSelectorViewModelDelegate.Parent {
            override val currentPosition: Int
                get() = this@StatisticsDetailViewModel.rangeDelegate.provideRangePosition()

            override fun onDateClick() {
                rangeDelegate.onSelectRangeClick()
            }

            override fun updatePosition(newPosition: Int) =
                this@StatisticsDetailViewModel.rangeDelegate.updatePosition(newPosition)

            override suspend fun getSetupData(): DateSelectorMapper.SetupData.Type {
                return DateSelectorMapper.SetupData.Type.Statistics(
                    optionsButton = dateSelectorViewModelDelegate.getOptionsButton(
                        options = statisticsDetailOptionsListMapper.map(
                            filterHidden = true,
                            rangeLength = rangeDelegate.provideRangeLength(),
                        ),
                    ),
                    rangeLength = rangeDelegate.provideRangeLength(),
                )
            }
        }
    }
}
