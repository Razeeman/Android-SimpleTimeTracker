package com.example.util.simpletimetracker.feature_records_all.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.extension.toParams
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.feature_base_adapter.record.RecordViewData
import com.example.util.simpletimetracker.feature_records_all.interactor.RecordsAllViewDataInteractor
import com.example.util.simpletimetracker.feature_records_all.mapper.RecordsAllViewDataMapper
import com.example.util.simpletimetracker.feature_records_all.model.RecordsAllSortOrder
import com.example.util.simpletimetracker.feature_records_all.viewData.RecordsAllSortOrderViewData
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordFromRecordsAllParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordParams
import com.example.util.simpletimetracker.navigation.params.screen.RecordsAllParams
import kotlinx.coroutines.launch
import javax.inject.Inject

class RecordsAllViewModel @Inject constructor(
    private val router: Router,
    private val recordsAllViewDataInteractor: RecordsAllViewDataInteractor,
    private val recordsAllViewDataMapper: RecordsAllViewDataMapper
) : ViewModel() {

    lateinit var extra: RecordsAllParams

    val records: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            viewModelScope.launch {
                initial.value = listOf(LoaderViewData())
                initial.value = loadRecordsViewData()
            }
            initial
        }
    }

    val sortOrderViewData: LiveData<RecordsAllSortOrderViewData> by lazy {
        MutableLiveData<RecordsAllSortOrderViewData>().let { initial ->
            viewModelScope.launch { initial.value = loadSortOrderViewData() }
            initial
        }
    }

    private var sortOrder: RecordsAllSortOrder = RecordsAllSortOrder.TIME_STARTED
    private var commentSearch: String = ""

    fun onRecordClick(item: RecordViewData, sharedElements: Pair<Any, String>) {
        if (item is RecordViewData.Tracked) {
            ChangeRecordParams.Tracked(
                transitionName = sharedElements.second,
                id = item.id,
                from = ChangeRecordParams.From.RecordsAll,
                preview = ChangeRecordParams.Preview(
                    name = item.name,
                    tagName = item.tagName,
                    timeStarted = item.timeStarted,
                    timeFinished = item.timeFinished,
                    duration = item.duration,
                    iconId = item.iconId.toParams(),
                    color = item.color,
                    comment = item.comment
                )
            ).let { params ->
                router.navigate(
                    data = ChangeRecordFromRecordsAllParams(params),
                    sharedElements = mapOf(sharedElements)
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
        records.set(listOf(LoaderViewData()))
        updateRecords()
        updateSortOrderViewData()
    }

    fun onCommentChange(text: String) {
        viewModelScope.launch {
            if (text != commentSearch) {
                commentSearch = text
                updateRecords()
            }
        }
    }

    private fun updateRecords() = viewModelScope.launch {
        val data = loadRecordsViewData()
        records.set(data)
    }

    private suspend fun loadRecordsViewData(): List<ViewHolderType> {
        return recordsAllViewDataInteractor.getViewData(
            filter = extra.filter,
            sortOrder = sortOrder,
            rangeStart = extra.rangeStart,
            rangeEnd = extra.rangeEnd,
            commentSearch = commentSearch,
        )
    }

    private fun updateSortOrderViewData() {
        val data = loadSortOrderViewData()
        sortOrderViewData.set(data)
    }

    private fun loadSortOrderViewData(): RecordsAllSortOrderViewData {
        return recordsAllViewDataMapper.toSortOrderViewData(sortOrder)
    }
}
