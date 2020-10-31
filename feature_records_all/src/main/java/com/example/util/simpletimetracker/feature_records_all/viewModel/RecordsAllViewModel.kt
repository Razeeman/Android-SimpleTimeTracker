package com.example.util.simpletimetracker.feature_records_all.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.core.view.TransitionNames
import com.example.util.simpletimetracker.core.viewData.RecordViewData
import com.example.util.simpletimetracker.feature_records_all.extra.RecordsAllExtra
import com.example.util.simpletimetracker.feature_records_all.interactor.RecordsAllViewDataInteractor
import com.example.util.simpletimetracker.feature_records_all.mapper.RecordsAllViewDataMapper
import com.example.util.simpletimetracker.feature_records_all.model.RecordsAllSortOrder
import com.example.util.simpletimetracker.feature_records_all.viewData.RecordsAllSortOrderViewData
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.Screen
import com.example.util.simpletimetracker.navigation.params.ChangeRecordParams
import com.example.util.simpletimetracker.navigation.params.TypesFilterDialogParams
import kotlinx.coroutines.launch
import javax.inject.Inject

class RecordsAllViewModel @Inject constructor(
    private val router: Router,
    private val recordsAllViewDataInteractor: RecordsAllViewDataInteractor,
    private val recordsAllViewDataMapper: RecordsAllViewDataMapper
) : ViewModel() {

    lateinit var extra: RecordsAllExtra

    val records: LiveData<List<ViewHolderType>> by lazy {
        updateRecords()
        MutableLiveData(listOf(LoaderViewData() as ViewHolderType))
    }

    val sortOrderViewData: LiveData<RecordsAllSortOrderViewData> by lazy {
        MutableLiveData<RecordsAllSortOrderViewData>().let { initial ->
            viewModelScope.launch { initial.value = loadSortOrderViewData() }
            initial
        }
    }

    private var sortOrder: RecordsAllSortOrder = RecordsAllSortOrder.TIME_STARTED
    private val typesSelected: MutableList<Long> by lazy { mutableListOf(extra.typeId) }

    fun onRecordClick(item: RecordViewData, sharedElements: Map<Any, String>) {
        if (item is RecordViewData.Tracked) {
            ChangeRecordParams.Tracked(
                transitionName = TransitionNames.RECORD + item.id,
                id = item.id
            ).let { params ->
                router.navigate(
                    screen = Screen.CHANGE_RECORD_FROM_RECORDS_ALL,
                    data = params,
                    sharedElements = sharedElements
                )
            }
        }
    }

    fun onVisible() {
        updateRecords()
    }

    fun onNeedUpdate() {
        updateRecords()
    }

    fun onRecordTypeOrderSelected(position: Int) {
        sortOrder = recordsAllViewDataMapper.toSortOrder(position)
        updateRecords()
    }

    fun onFilterClick() {
        router.navigate(
            Screen.TYPES_FILTER_DIALOG,
            TypesFilterDialogParams(typesSelected)
        )
    }

    fun onTypesSelected(newTypes: List<Long>) {
        typesSelected.clear()
        typesSelected.addAll(newTypes)
        updateRecords()
    }

    private fun updateRecords() = viewModelScope.launch {
        (records as MutableLiveData).value = loadRecordsViewData()
    }

    private suspend fun loadRecordsViewData(): List<ViewHolderType> {
        return recordsAllViewDataInteractor.getViewData(typesSelected, sortOrder)
    }

    private fun loadSortOrderViewData(): RecordsAllSortOrderViewData {
        return recordsAllViewDataMapper.toSortOrderViewData(sortOrder)
    }
}
