package com.example.util.simpletimetracker.feature_dialogs.chartFilter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.navigation.Router
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChartFilterViewModel @Inject constructor(
    private val router: Router,
    private val recordInteractor: RecordInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val chartFilterViewDataMapper: ChartFilterViewDataMapper
) : ViewModel() {

    val recordTypes: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            viewModelScope.launch { initial.value = loadRecordTypesViewData() }
            initial
        }
    }

    fun onRecordTypeClick(item: ChartFilterRecordTypeViewData) {
        router.showSystemMessage("Clicked!")
    }

    private suspend fun updateRecordTypes() {
        (recordTypes as MutableLiveData).value = loadRecordTypesViewData()
    }

    private suspend fun loadRecordTypesViewData(): List<ViewHolderType> {
        val typesInStatistics = recordInteractor.getAll()
            .map(Record::typeId)
            .toSet()

        return recordTypeInteractor.getAll()
            .filter { it.id in typesInStatistics }
            .map(chartFilterViewDataMapper::map)
    }
}
