package com.example.util.simpletimetracker.feature_widget.universal.activity.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.core.interactor.AddRunningRecordMediator
import com.example.util.simpletimetracker.core.interactor.RemoveRunningRecordMediator
import com.example.util.simpletimetracker.core.viewData.RecordTypeViewData
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.model.RunningRecord
import com.example.util.simpletimetracker.feature_widget.universal.mapper.WidgetUniversalViewDataMapper
import kotlinx.coroutines.launch
import javax.inject.Inject

class WidgetUniversalViewModel @Inject constructor(
    private val addRunningRecordMediator: AddRunningRecordMediator,
    private val removeRunningRecordMediator: RemoveRunningRecordMediator,
    private val recordInteractor: RecordInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val widgetUniversalViewDataMapper: WidgetUniversalViewDataMapper
) : ViewModel() {

    val recordTypes: LiveData<List<ViewHolderType>> by lazy {
        updateRecordTypesViewData()
        MutableLiveData(listOf(LoaderViewData() as ViewHolderType))
    }

    fun onRecordTypeClick(item: RecordTypeViewData) {
        viewModelScope.launch {
            val runningRecord = runningRecordInteractor.get(item.id)
            if (runningRecord != null) {
                // Stop running record, add new record
                handleRunningRecordRemove(runningRecord)
            } else {
                // Stop other activities if necessary
                if (!prefsInteractor.getAllowMultitasking()) {
                    runningRecordInteractor.getAll().forEach { handleRunningRecordRemove(it) }
                }
                // Add new running record
                addRunningRecordMediator.add(item.id)
            }

            updateRecordTypesViewData()
        }
    }

    private suspend fun handleRunningRecordRemove(runningRecord: RunningRecord) {
        recordInteractor.add(
            typeId = runningRecord.id,
            timeStarted = runningRecord.timeStarted
        )
        removeRunningRecordMediator.remove(runningRecord.id)
    }

    private fun updateRecordTypesViewData() = viewModelScope.launch {
        val data = loadRecordTypesViewData()
        (recordTypes as MutableLiveData).value = data
    }

    private suspend fun loadRecordTypesViewData(): List<ViewHolderType> {
        val runningRecords = runningRecordInteractor.getAll()
        val recordTypesRunning = runningRecords.map { it.id }
        val numberOfCards = prefsInteractor.getNumberOfCards()
        val isDarkTheme = prefsInteractor.getDarkMode()

        return recordTypeInteractor.getAll()
            .filterNot { it.hidden }
            .map {
                widgetUniversalViewDataMapper.map(
                    recordType = it,
                    isFiltered = it.id in recordTypesRunning,
                    numberOfCards = numberOfCards,
                    isDarkTheme = isDarkTheme
                )
            }
            .takeUnless { it.isEmpty() }
            ?: listOf(widgetUniversalViewDataMapper.mapToEmpty())
    }
}
