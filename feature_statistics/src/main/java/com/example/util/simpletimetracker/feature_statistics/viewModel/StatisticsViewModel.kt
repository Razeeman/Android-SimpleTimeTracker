package com.example.util.simpletimetracker.feature_statistics.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.model.ChartFilterType
import com.example.util.simpletimetracker.feature_statistics.extra.StatisticsExtra
import com.example.util.simpletimetracker.feature_statistics.interactor.StatisticsViewDataInteractor
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.feature_statistics.viewData.StatisticsViewData
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.Screen
import com.example.util.simpletimetracker.navigation.params.StatisticsDetailParams
import kotlinx.coroutines.launch
import javax.inject.Inject

class StatisticsViewModel @Inject constructor(
    private val router: Router,
    private val statisticsViewDataInteractor: StatisticsViewDataInteractor
) : ViewModel() {

    var extra: StatisticsExtra? = null

    val statistics: LiveData<List<ViewHolderType>> by lazy {
        MutableLiveData(listOf(LoaderViewData() as ViewHolderType))
    }

    private var rangeLength: RangeLength? = null
    private var isVisible: Boolean = false

    fun onVisible() {
        isVisible = true
        updateStatistics()
    }

    fun onHidden() {
        isVisible = false
    }

    fun onNewRange(newRangeLength: RangeLength) {
        rangeLength = newRangeLength
        if (isVisible) updateStatistics()
    }

    fun onFilterClick() {
        router.navigate(Screen.CHART_FILTER_DIALOG)
    }

    fun onItemClick(item: StatisticsViewData, sharedElements: Map<Any, String>) {
        // TODO untracked detailed statistics
        if (item.id == -1L) return

        val filterType = when (item) {
            is StatisticsViewData.Activity -> ChartFilterType.ACTIVITY
            is StatisticsViewData.Category -> ChartFilterType.CATEGORY
        }

        router.navigate(
            screen = Screen.STATISTICS_DETAIL,
            data = StatisticsDetailParams(
                id = item.id,
                filterType = filterType,
                preview = StatisticsDetailParams.Preview(
                    name = item.name,
                    iconId = (item as? StatisticsViewData.Activity)?.iconId,
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
        (statistics as MutableLiveData).value = data
    }

    private suspend fun loadStatisticsViewData(): List<ViewHolderType> {
        return statisticsViewDataInteractor.getViewData(rangeLength, extra?.shift.orZero())
    }
}
