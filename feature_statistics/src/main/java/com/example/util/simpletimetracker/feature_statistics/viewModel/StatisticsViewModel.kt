package com.example.util.simpletimetracker.feature_statistics.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.feature_statistics.adapter.StatisticsViewData
import com.example.util.simpletimetracker.feature_statistics.mapper.StatisticsViewDataMapper
import kotlinx.coroutines.launch
import javax.inject.Inject

class StatisticsViewModel : ViewModel() {

    @Inject
    lateinit var recordInteractor: RecordInteractor
    @Inject
    lateinit var recordTypeInteractor: RecordTypeInteractor
    @Inject
    lateinit var statisticsViewDataMapper: StatisticsViewDataMapper

    private val statisticsLiveData: MutableLiveData<List<StatisticsViewData>> by lazy {
        return@lazy MutableLiveData<List<StatisticsViewData>>().let { initial ->
            viewModelScope.launch { initial.value = loadStatisticsViewData() }
            initial
        }
    }

    val statistics: LiveData<List<StatisticsViewData>>
        get() = statisticsLiveData

    fun onVisible() {
        viewModelScope.launch {
            updateStatistics()
        }
    }

    private suspend fun updateStatistics() {
        statisticsLiveData.value = loadStatisticsViewData()
    }

    private suspend fun loadStatisticsViewData(): List<StatisticsViewData> {
        return statisticsViewDataMapper.map(
            records = recordInteractor.getAll(),
            recordTypes = recordTypeInteractor.getAll()
        )
    }
}
