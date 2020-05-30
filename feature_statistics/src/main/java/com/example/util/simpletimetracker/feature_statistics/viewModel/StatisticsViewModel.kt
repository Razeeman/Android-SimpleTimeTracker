package com.example.util.simpletimetracker.feature_statistics.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.StatisticsInteractor
import com.example.util.simpletimetracker.feature_statistics.extra.StatisticsExtra
import com.example.util.simpletimetracker.feature_statistics.mapper.StatisticsViewDataMapper
import kotlinx.coroutines.launch
import javax.inject.Inject

class StatisticsViewModel @Inject constructor(
    private val recordTypeInteractor: RecordTypeInteractor,
    private val statisticsInteractor: StatisticsInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val statisticsViewDataMapper: StatisticsViewDataMapper
) : ViewModel() {

    var extra: StatisticsExtra? = null

    val statistics: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            viewModelScope.launch { initial.value = loadStatisticsViewData() }
            initial
        }
    }

    fun onVisible() {
        viewModelScope.launch {
            updateStatistics()
        }
    }

    fun onFilterApplied() {
        viewModelScope.launch {
            updateStatistics()
        }
    }

    private suspend fun updateStatistics() {
        (statistics as MutableLiveData).value = loadStatisticsViewData()
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
