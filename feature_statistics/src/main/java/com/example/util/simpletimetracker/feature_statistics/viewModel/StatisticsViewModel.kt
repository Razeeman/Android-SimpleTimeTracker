package com.example.util.simpletimetracker.feature_statistics.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.StatisticsInteractor
import com.example.util.simpletimetracker.feature_statistics.extra.StatisticsExtra
import com.example.util.simpletimetracker.feature_statistics.mapper.StatisticsViewDataMapper
import kotlinx.coroutines.launch
import javax.inject.Inject

class StatisticsViewModel @Inject constructor(
    private var recordTypeInteractor: RecordTypeInteractor,
    private var statisticsInteractor: StatisticsInteractor,
    private var statisticsViewDataMapper: StatisticsViewDataMapper
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

    private suspend fun updateStatistics() {
        (statistics as MutableLiveData).value = loadStatisticsViewData()
    }

    private suspend fun loadStatisticsViewData(): List<ViewHolderType> {
        val statistics = if (extra?.start.orZero() != 0L && extra?.end.orZero() != 0L) {
            statisticsInteractor.getFromRange(extra?.start.orZero(), extra?.end.orZero())
        } else {
            statisticsInteractor.getAll()
        }
        val types = recordTypeInteractor.getAll()

        val list = statisticsViewDataMapper.map(statistics, types)
        val chart = statisticsViewDataMapper.mapToChart(statistics, types)

        return mutableListOf(chart).apply { addAll(list) }
    }
}
