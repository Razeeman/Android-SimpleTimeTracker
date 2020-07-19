package com.example.util.simpletimetracker.feature_statistics_detail.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.feature_statistics_detail.extra.StatisticsDetailExtra
import com.example.util.simpletimetracker.feature_statistics_detail.interactor.StatisticsDetailInteractor
import com.example.util.simpletimetracker.feature_statistics_detail.mapper.StatisticsDetailViewDataMapper
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailViewData
import kotlinx.coroutines.launch
import javax.inject.Inject

class StatisticsDetailViewModel @Inject constructor(
    private val recordInteractor: RecordInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val statisticsDetailInteractor: StatisticsDetailInteractor,
    private val statisticsDetailViewDataMapper: StatisticsDetailViewDataMapper
) : ViewModel() {

    lateinit var extra: StatisticsDetailExtra

    val viewData: LiveData<StatisticsDetailViewData> by lazy {
        return@lazy MutableLiveData<StatisticsDetailViewData>().let { initial ->
            viewModelScope.launch { initial.value = loadPreviewViewData() }
            initial
        }
    }

    private suspend fun loadPreviewViewData(): StatisticsDetailViewData {
        return if (extra.typeId == -1L) {
            statisticsDetailViewDataMapper.mapToUntracked()
        } else {
            val records = recordInteractor.getAll() // TODO get by typeId
                .filter { it.typeId == extra.typeId }
            val recordType = recordTypeInteractor.get(extra.typeId)
            val durations = statisticsDetailInteractor.getDurations(extra.typeId, 14)
            statisticsDetailViewDataMapper.map(records, recordType, durations)
        }
    }
}
