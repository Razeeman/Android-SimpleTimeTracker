package com.example.util.simpletimetracker.feature_statistics_detail.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.BaseViewModel
import com.example.util.simpletimetracker.core.base.SingleLiveEvent
import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.core.delegates.dateSelector.mapper.DateSelectorMapper
import com.example.util.simpletimetracker.core.delegates.dateSelector.viewModelDelegate.DateSelectorViewModelDelegate
import com.example.util.simpletimetracker.core.extension.lazySuspend
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.extension.toParams
import com.example.util.simpletimetracker.core.viewData.RangeSelectionOptionsListItem
import com.example.util.simpletimetracker.domain.base.Coordinates
import com.example.util.simpletimetracker.domain.record.model.Range
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.record.model.RecordsFilter
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.buttonsRow.ButtonsRowItemViewData
import com.example.util.simpletimetracker.feature_base_adapter.buttonsRow.view.ButtonsRowViewData
import com.example.util.simpletimetracker.feature_base_adapter.statistics.StatisticsViewData
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailBlock
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailPreviewsViewData
import com.example.util.simpletimetracker.feature_statistics_detail.customView.SeriesCalendarView
import com.example.util.simpletimetracker.feature_statistics_detail.interactor.StatisticsDetailContentInteractor
import com.example.util.simpletimetracker.feature_statistics_detail.mapper.StatisticsDetailOptionsListMapper
import com.example.util.simpletimetracker.feature_statistics_detail.model.DataDistributionMode
import com.example.util.simpletimetracker.feature_statistics_detail.model.StatisticsDetailOptionsListItem
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
    private val statsDelegate: StatisticsDetailStatsViewModelDelegate,
    private val streaksDelegate: StatisticsDetailStreaksViewModelDelegate,
    private val chartDelegate: StatisticsDetailChartViewModelDelegate,
    private val splitChartDelegate: StatisticsDetailSplitChartViewModelDelegate,
    private val nextActivitiesDelegate: StatisticsDetailNextActivitiesViewModelDelegate,
    private val durationSplitDelegate: StatisticsDetailDurationSplitViewModelDelegate,
    private val rangeDelegate: StatisticsDetailRangeViewModelDelegate,
    private val filterDelegate: StatisticsDetailFilterViewModelDelegate,
    private val dailyCalendarDelegate: StatisticsDetailDailyCalendarViewModelDelegate,
    private val goalsDelegate: StatisticsDetailGoalsViewModelDelegate,
    private val dataDistributionDelegate: StatisticsDetailDataDistributionViewModelDelegate,
    private val tagValueDelegate: StatisticsDetailTagValueViewModelDelegate,
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
        rangeDelegate.initialize(extra)
        filterDelegate.initialize(extra)
        viewModelScope.launch {
            dateSelectorViewModelDelegate.initialize(rangeDelegate.provideRangePosition())
        }
    }

    fun onVisible() {
        filterDelegate.onVisible()
        // TODO update only when necessary?
        viewModelScope.launch {
            dateSelectorViewModelDelegate.setup()
            dateSelectorViewModelDelegate.updatePosition(rangeDelegate.provideRangePosition())
        }
    }

    fun onTypesFilterSelected(result: RecordsFilterResultParams) {
        filterDelegate.onTypesFilterSelected(result)
    }

    fun onTypesFilterDismissed(tag: String) {
        filterDelegate.onTypesFilterDismissed(tag)
    }

    fun onButtonsRowClick(
        block: ButtonsRowItemViewData.ButtonsRowId,
        viewData: ButtonsRowViewData,
    ) {
        when (block) {
            StatisticsDetailBlock.ChartGrouping ->
                chartDelegate.onChartGroupingClick(viewData)
            StatisticsDetailBlock.ChartLength ->
                chartDelegate.onChartLengthClick(viewData)
            StatisticsDetailBlock.GoalChartGrouping ->
                goalsDelegate.onChartGroupingClick(viewData)
            StatisticsDetailBlock.GoalChartLength ->
                goalsDelegate.onChartLengthClick(viewData)
            StatisticsDetailBlock.TagValuesChartGrouping ->
                tagValueDelegate.onChartGroupingClick(viewData)
            StatisticsDetailBlock.TagValuesChartLength ->
                tagValueDelegate.onChartLengthClick(viewData)
            StatisticsDetailBlock.TagValuesChartMode ->
                tagValueDelegate.onChartTagValueModeClick(viewData)
            StatisticsDetailBlock.SeriesGoal ->
                streaksDelegate.onStreaksGoalClick(viewData)
            StatisticsDetailBlock.SeriesType ->
                streaksDelegate.onStreaksTypeClick(viewData)
            StatisticsDetailBlock.SplitChartGrouping ->
                splitChartDelegate.onSplitChartGroupingClick(viewData)
            StatisticsDetailBlock.DataDistributionMode ->
                dataDistributionDelegate.onDataDistributionModeClick(viewData)
            StatisticsDetailBlock.DataDistributionGraph ->
                dataDistributionDelegate.onDataDistributionGraphClick(viewData)
            else -> {
                // Do nothing
            }
        }
    }

    fun onButtonClick(block: StatisticsDetailBlock) {
        when (block) {
            StatisticsDetailBlock.ChartSplitByActivity ->
                chartDelegate.onSplitByActivityClick()
            StatisticsDetailBlock.ChartSplitByActivitySort ->
                chartDelegate.onSplitByActivitySortClick()
            StatisticsDetailBlock.TagValuesMultiplyDuration ->
                tagValueDelegate.onMultiplyDurationClick()
            else -> {
                // Do nothing
            }
        }
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
        dataDistributionDelegate.onStatisticsItemClick(item)
    }

    fun onPreviewItemClick(item: StatisticsDetailPreview) {
        previewDelegate.onPreviewItemClick(item)
        filterDelegate.onPreviewItemClick(item)
    }

    fun onPreviewItemLongClick(item: StatisticsDetailPreview) {
        filterDelegate.onPreviewItemLongClick(item)
    }

    fun onChartClick(block: StatisticsDetailBlock, barId: Long?) {
        when (block) {
            StatisticsDetailBlock.DataDistributionBarChart ->
                dataDistributionDelegate.onChartClick(barId)
            StatisticsDetailBlock.DataDistributionPieChart ->
                dataDistributionDelegate.onChartClick(barId)
            else -> {
                // Do nothing
            }
        }
    }

    fun onSwipedStart(item: ViewHolderType?) {
        item ?: return
        dataDistributionDelegate.onStatisticsItemSwipedStart(item)
    }

    fun onSwipedEnd(item: ViewHolderType?) {
        item ?: return
        dataDistributionDelegate.onStatisticsItemSwipedEnd(item)
    }

    fun onOptionsClick() = viewModelScope.launch {
        val rangeLength = rangeDelegate.provideRangeLength()
        val items = statisticsDetailOptionsListMapper.map(rangeLength)
        router.navigate(OptionsListParams(items))
    }

    fun onOptionsLongClick() {
        filterDelegate.onFilterClick()
    }

    fun onOptionsItemClick(id: StatisticsDetailOptionsListItem) {
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

    fun onStreaksCalendarClick(
        viewData: SeriesCalendarView.ViewData,
        coordinates: Coordinates,
    ) {
        streaksDelegate.onStreaksCalendarClick(viewData, coordinates)
    }

    fun onBackPressed() {
        router.back()
    }

    private fun onRecordsClick() {
        val finalFilters = filterDelegate.provideFilter()
            .plus(rangeDelegate.getDateFilter())
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
        previewDelegate.updateViewData()
        statsDelegate.updateViewData()
        streaksDelegate.updateStreaksViewData()
        chartDelegate.updateViewData()
        dailyCalendarDelegate.updateViewData()
        splitChartDelegate.updateSplitChartViewData()
        durationSplitDelegate.updateViewData()
        nextActivitiesDelegate.updateViewData()
        goalsDelegate.updateViewData()
        dataDistributionDelegate.updateViewData()
        tagValueDelegate.updateViewData()
        dateSelectorViewModelDelegate.updatePosition(rangeDelegate.provideRangePosition())
    }

    private fun updateContent() {
        val data = loadContent()
        content.set(data)
        checkTopScroll(data)
    }

    // TODO move to delegates
    private fun loadContent(): List<ViewHolderType> {
        return statisticsDetailContentInteractor.getContent(
            previewViewData = previewViewData.value,
            chartViewData = chartDelegate.viewData.value,
            dailyCalendarViewData = dailyCalendarDelegate.viewData.value,
            statsViewData = statsDelegate.viewData.value,
            streaksViewData = streaksDelegate.streaksViewData.value,
            streaksGoalViewData = streaksDelegate.streaksGoalViewData.value,
            streaksTypeViewData = streaksDelegate.streaksTypeViewData.value,
            splitChartViewData = splitChartDelegate.splitChartViewData.value,
            comparisonSplitChartViewData = splitChartDelegate.comparisonSplitChartViewData.value,
            splitChartGroupingViewData = splitChartDelegate.splitChartGroupingViewData.value,
            durationSplitChartViewData = durationSplitDelegate.viewData.value,
            comparisonDurationSplitChartViewData = durationSplitDelegate.comparisonViewData.value,
            nextActivitiesViewData = nextActivitiesDelegate.viewData.value,
            goalsViewData = goalsDelegate.viewData.value,
            dataDistributionViewData = dataDistributionDelegate.viewData.value,
            tagValueViewData = tagValueDelegate.viewData.value,
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

            override suspend fun onRangeChanged() {
                dateSelectorViewModelDelegate.setup()
                splitChartDelegate.updateSplitChartGroupingViewData()
                streaksDelegate.updateStreaksGoalViewData()
                dailyCalendarDelegate.updateViewData()
            }

            override fun updateViewData() {
                this@StatisticsDetailViewModel.updateViewData()
            }

            override fun getDateFilter(): List<RecordsFilter> {
                return rangeDelegate.getDateFilter()
            }

            override suspend fun onFiltersChanged() {
                streaksDelegate.onTypesFilterDismissed()
                previewDelegate.updateViewData()
                streaksDelegate.updateStreaksGoalViewData()
                updateViewData()
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
                    rangeLength = rangeDelegate.provideRangeLength(),
                )
            }
        }
    }
}
