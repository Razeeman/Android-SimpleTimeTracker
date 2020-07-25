package com.example.util.simpletimetracker.feature_statistics.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.core.utils.CountingIdlingResourceProvider
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.StatisticsInteractor
import com.example.util.simpletimetracker.feature_statistics.extra.StatisticsExtra
import com.example.util.simpletimetracker.feature_statistics.mapper.StatisticsViewDataMapper
import com.example.util.simpletimetracker.feature_statistics.viewData.StatisticsViewData
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.Screen
import com.example.util.simpletimetracker.navigation.params.StatisticsDetailParams
import kotlinx.coroutines.launch
import javax.inject.Inject

class StatisticsViewModel @Inject constructor(
    private val router: Router,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val statisticsInteractor: StatisticsInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val statisticsViewDataMapper: StatisticsViewDataMapper
) : ViewModel() {

    var extra: StatisticsExtra? = null

    val statistics: LiveData<List<ViewHolderType>> by lazy {
        updateStatistics()
        MutableLiveData(listOf(LoaderViewData() as ViewHolderType))
    }

    fun onVisible() {
        updateStatistics()
    }

    fun onFilterClick() {
        router.navigate(Screen.CHART_FILTER_DIALOG)
    }

    fun onItemClick(item: StatisticsViewData, sharedElements: Map<Any, String>) {
        if (item.typeId == -1L) return // TODO untracked detailed statistics

        router.navigate(
            screen = Screen.STATISTICS_DETAIL,
            data = StatisticsDetailParams(item.typeId),
            sharedElements = sharedElements
        )
    }

    fun onFilterApplied() {
        updateStatistics()
    }

    private fun updateStatistics() = viewModelScope.launch {
        CountingIdlingResourceProvider.increment()
        (statistics as MutableLiveData).value = loadStatisticsViewData()
        CountingIdlingResourceProvider.decrement()
    }

    private suspend fun loadStatisticsViewData(): List<ViewHolderType> {
        val showDuration: Boolean
        val types = recordTypeInteractor.getAll()
        val typesFiltered = prefsInteractor.getFilteredTypes()
        val statistics = if (extra?.start.orZero() != 0L && extra?.end.orZero() != 0L) {
            showDuration = true
            statisticsInteractor.getFromRange(
                start = extra?.start.orZero(),
                end = extra?.end.orZero(),
                addUntracked = !typesFiltered.contains(-1L)
            )
        } else {
            showDuration = false
            statisticsInteractor.getAll()
        }

        val list = statisticsViewDataMapper.map(statistics, types, typesFiltered, showDuration)
        val chart = statisticsViewDataMapper.mapToChart(statistics, types, typesFiltered)

        if (list.isEmpty()) return listOf(statisticsViewDataMapper.mapToEmpty())
        return listOf(chart) + list
    }
}
