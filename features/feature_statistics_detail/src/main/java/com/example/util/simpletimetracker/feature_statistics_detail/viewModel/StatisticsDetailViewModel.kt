package com.example.util.simpletimetracker.feature_statistics_detail.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.BaseViewModel
import com.example.util.simpletimetracker.core.base.SingleLiveEvent
import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.core.extension.lazySuspend
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.extension.toParams
import com.example.util.simpletimetracker.core.view.buttonsRowView.ButtonsRowViewData
import com.example.util.simpletimetracker.core.viewData.RangesViewData
import com.example.util.simpletimetracker.domain.model.Coordinates
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.domain.model.RecordBase
import com.example.util.simpletimetracker.domain.model.RecordsFilter
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailBlock
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailPreviewsViewData
import com.example.util.simpletimetracker.feature_statistics_detail.customView.SeriesCalendarView
import com.example.util.simpletimetracker.feature_statistics_detail.interactor.StatisticsDetailContentInteractor
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailCardInternalViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailClickableLongest
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailClickableShortest
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailClickableTracked
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailPreviewCompositeViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewModel.delegate.StatisticsDetailChartViewModelDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.viewModel.delegate.StatisticsDetailDurationSplitViewModelDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.viewModel.delegate.StatisticsDetailFilterViewModelDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.viewModel.delegate.StatisticsDetailNextActivitiesViewModelDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.viewModel.delegate.StatisticsDetailPreviewViewModelDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.viewModel.delegate.StatisticsDetailRangeViewModelDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.viewModel.delegate.StatisticsDetailSplitChartViewModelDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.viewModel.delegate.StatisticsDetailStatsViewModelDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.viewModel.delegate.StatisticsDetailStreaksViewModelDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.viewModel.delegate.StatisticsDetailViewModelDelegate
import com.example.util.simpletimetracker.feature_views.spinner.CustomSpinner
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.notification.PopupParams
import com.example.util.simpletimetracker.navigation.params.screen.RecordsAllParams
import com.example.util.simpletimetracker.navigation.params.screen.RecordsFilterResultParams
import com.example.util.simpletimetracker.navigation.params.screen.StatisticsDetailParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatisticsDetailViewModel @Inject constructor(
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
) : BaseViewModel() {

    val scrollToTop: LiveData<Unit> = SingleLiveEvent()
    val content: LiveData<List<ViewHolderType>> by lazySuspend { loadContent() }
    val title: LiveData<String> by rangeDelegate::title
    val rangeItems: LiveData<RangesViewData> by rangeDelegate::rangeItems
    val rangeButtonsVisibility: LiveData<Boolean> by rangeDelegate::rangeButtonsVisibility
    val previewViewData: LiveData<StatisticsDetailPreviewCompositeViewData?> by previewDelegate::viewData

    private lateinit var extra: StatisticsDetailParams

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
    )

    init {
        val delegateParent = getDelegateParent()
        delegates.forEach { it.attach(delegateParent) }
    }

    override fun onCleared() {
        delegates.forEach { (it as? ViewModelDelegate)?.clear() }
        super.onCleared()
    }

    fun initialize(extra: StatisticsDetailParams) {
        if (this::extra.isInitialized) return
        this.extra = extra
        rangeDelegate.initialize(extra)
    }

    fun onVisible() {
        filterDelegate.onVisible()
    }

    fun onFilterClick() {
        filterDelegate.onFilterClick()
    }

    fun onCompareClick() {
        filterDelegate.onCompareClick()
    }

    fun onTypesFilterSelected(result: RecordsFilterResultParams) {
        filterDelegate.onTypesFilterSelected(result)
    }

    fun onTypesFilterDismissed(tag: String) {
        filterDelegate.onTypesFilterDismissed(tag)
    }

    fun onButtonsRowClick(block: StatisticsDetailBlock, viewData: ButtonsRowViewData) {
        when (block) {
            StatisticsDetailBlock.ChartGrouping ->
                chartDelegate.onChartGroupingClick(viewData)
            StatisticsDetailBlock.ChartLength ->
                chartDelegate.onChartLengthClick(viewData)
            StatisticsDetailBlock.SeriesGoal ->
                streaksDelegate.onStreaksGoalClick(viewData)
            StatisticsDetailBlock.SeriesType ->
                streaksDelegate.onStreaksTypeClick(viewData)
            StatisticsDetailBlock.SplitChartGrouping ->
                splitChartDelegate.onSplitChartGroupingClick(viewData)
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
            is StatisticsDetailClickableShortest -> {
                PopupParams(
                    message = type.message,
                    anchorCoordinates = coordinates,
                ).let(router::show)
            }
            is StatisticsDetailClickableLongest -> {
                PopupParams(
                    message = type.message,
                    anchorCoordinates = coordinates,
                ).let(router::show)
            }
        }
    }

    fun onPreviousClick() {
        rangeDelegate.onPreviousClick()
    }

    fun onTodayClick() {
        rangeDelegate.onTodayClick()
    }

    fun onNextClick() {
        rangeDelegate.onNextClick()
    }

    fun onRangeSelected(item: CustomSpinner.CustomSpinnerItem) {
        rangeDelegate.onRangeSelected(item)
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

    private fun onRecordsClick() {
        viewModelScope.launch {
            val finalFilters = filterDelegate.provideFilter()
                .plus(rangeDelegate.getDateFilter())
                .map(RecordsFilter::toParams).toList()

            router.navigate(RecordsAllParams(finalFilters))
        }
    }

    private fun checkTopScroll(
        oldData: List<ViewHolderType>,
        newData: List<ViewHolderType>,
    ) {
        val previewsWillBeShown = oldData.none { it is StatisticsDetailPreviewsViewData } &&
            newData.any { it is StatisticsDetailPreviewsViewData }
        if (previewsWillBeShown) {
            scrollToTop.set(Unit)
        }
    }

    private fun updateViewData() {
        statsDelegate.updateViewData()
        streaksDelegate.updateStreaksViewData()
        chartDelegate.updateViewData()
        splitChartDelegate.updateSplitChartViewData()
        durationSplitDelegate.updateViewData()
        nextActivitiesDelegate.updateViewData()
    }

    private fun updateContent() {
        val oldData = content.value.orEmpty()
        val data = loadContent()
        content.set(data)
        checkTopScroll(oldData, data)
    }

    // TODO move to delegates
    private fun loadContent(): List<ViewHolderType> {
        return statisticsDetailContentInteractor.getContent(
            previewViewData = previewViewData.value,
            chartViewData = chartDelegate.viewData.value,
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

            override fun onRangeChanged() {
                splitChartDelegate.updateSplitChartGroupingViewData()
                streaksDelegate.updateStreaksGoalViewData()
            }

            override fun updateViewData() {
                this@StatisticsDetailViewModel.updateViewData()
            }

            override suspend fun getDateFilter(): List<RecordsFilter> {
                return rangeDelegate.getDateFilter()
            }

            override suspend fun onTypesFilterDismissed() {
                streaksDelegate.onTypesFilterDismissed()
                previewDelegate.updateViewData()
                streaksDelegate.updateStreaksGoalViewData()
                updateViewData()
            }
        }
    }
}
