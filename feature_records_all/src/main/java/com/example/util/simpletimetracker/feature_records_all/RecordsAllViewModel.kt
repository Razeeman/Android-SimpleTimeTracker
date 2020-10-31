package com.example.util.simpletimetracker.feature_records_all

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.core.view.TransitionNames
import com.example.util.simpletimetracker.core.viewData.RecordViewData
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.Screen
import com.example.util.simpletimetracker.navigation.params.ChangeRecordParams
import kotlinx.coroutines.launch
import javax.inject.Inject

class RecordsAllViewModel @Inject constructor(
    private val router: Router,
    private val recordsAllViewDataInteractor: RecordsAllViewDataInteractor
) : ViewModel() {

    lateinit var extra: RecordsAllExtra

    val records: LiveData<List<ViewHolderType>> by lazy {
        updateRecords()
        MutableLiveData(listOf(LoaderViewData() as ViewHolderType))
    }

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

    private fun updateRecords() = viewModelScope.launch {
        (records as MutableLiveData).value = loadRecordsViewData()
    }

    private suspend fun loadRecordsViewData(): List<ViewHolderType> {
        return recordsAllViewDataInteractor.getViewData(extra.typeId)
    }
}
