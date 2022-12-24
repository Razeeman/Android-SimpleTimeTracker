package com.example.util.simpletimetracker.feature_statistics.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.extension.toParams
import com.example.util.simpletimetracker.core.utils.UNTRACKED_ITEM_ID
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.feature_base_adapter.statistics.StatisticsViewData
import com.example.util.simpletimetracker.feature_statistics.extra.StatisticsExtra
import com.example.util.simpletimetracker.feature_statistics.interactor.StatisticsViewDataInteractor
import com.example.util.simpletimetracker.feature_views.TransitionNames
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.ChartFilterDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.StatisticsDetailParams
import com.example.util.simpletimetracker.navigation.params.screen.TypesFilterParams
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

class StatisticsViewModel @Inject constructor(
    private val router: Router,
    private val statisticsViewDataInteractor: StatisticsViewDataInteractor,
    private val prefsInteractor: PrefsInteractor,
) : ViewModel() {

    var extra: StatisticsExtra? = null

    val statistics: LiveData<List<ViewHolderType>> by lazy {
        MutableLiveData(listOf(LoaderViewData() as ViewHolderType))
    }

    private var isVisible: Boolean = false
    private var timerJob: Job? = null
    private val shift: Int get() = extra?.shift.orZero()

    fun onVisible() {
        isVisible = true
        if (shift == 0) {
            startUpdate()
        } else {
            updateStatistics()
        }
    }

    fun onHidden() {
        isVisible = false
        stopUpdate()
    }

    fun onRangeUpdated() {
        if (isVisible) updateStatistics()
    }

    fun onFilterClick() {
        router.navigate(ChartFilterDialogParams)
    }

    fun onItemClick(
        item: StatisticsViewData,
        sharedElements: Map<Any, String>,
    ) = viewModelScope.launch {
        // TODO untracked detailed statistics
        if (item.id == UNTRACKED_ITEM_ID) return@launch

        val filterType = prefsInteractor.getChartFilterType()
        val rangeLength = if (prefsInteractor.getKeepStatisticsRange()) {
            prefsInteractor.getStatisticsRange()
        } else {
            prefsInteractor.getStatisticsDetailRange()
        }

        router.navigate(
            data = StatisticsDetailParams(
                transitionName = TransitionNames.STATISTICS_DETAIL + item.id,
                filter = TypesFilterParams(
                    selectedIds = listOf(item.id),
                    filterType = filterType
                ),
                range = when (rangeLength) {
                    is RangeLength.Day -> StatisticsDetailParams.RangeLengthParams.Day
                    is RangeLength.Week -> StatisticsDetailParams.RangeLengthParams.Week
                    is RangeLength.Month -> StatisticsDetailParams.RangeLengthParams.Month
                    is RangeLength.Year -> StatisticsDetailParams.RangeLengthParams.Year
                    is RangeLength.All -> StatisticsDetailParams.RangeLengthParams.All
                    is RangeLength.Custom -> StatisticsDetailParams.RangeLengthParams.Custom(
                        start = rangeLength.range.timeStarted,
                        end = rangeLength.range.timeEnded,
                    )
                    is RangeLength.Last -> StatisticsDetailParams.RangeLengthParams.Last
                },
                shift = if (prefsInteractor.getKeepStatisticsRange()) shift else 0,
                preview = StatisticsDetailParams.Preview(
                    name = item.name,
                    iconId = item.icon?.toParams(),
                    color = item.color
                )
            ),
            sharedElements = sharedElements
        )
    }

    fun onFilterApplied() {
        updateStatistics()
    }

    private fun updateStatistics() = viewModelScope.launch {
        val data = loadStatisticsViewData()
        statistics.set(data)
    }

    private suspend fun loadStatisticsViewData(): List<ViewHolderType> {
        return statisticsViewDataInteractor.getViewData(
            rangeLength = prefsInteractor.getStatisticsRange(),
            shift = shift,
        )
    }

    private fun startUpdate() {
        timerJob = viewModelScope.launch {
            timerJob?.cancelAndJoin()
            while (isActive) {
                updateStatistics()
                delay(TIMER_UPDATE)
            }
        }
    }

    private fun stopUpdate() {
        viewModelScope.launch {
            timerJob?.cancelAndJoin()
        }
    }

    companion object {
        private const val TIMER_UPDATE = 1000L
    }
}
