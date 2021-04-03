package com.example.util.simpletimetracker.feature_records.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.core.view.TransitionNames
import com.example.util.simpletimetracker.core.viewData.RecordViewData
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.feature_records.extra.RecordsExtra
import com.example.util.simpletimetracker.feature_records.interactor.RecordsViewDataInteractor
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.Screen
import com.example.util.simpletimetracker.navigation.params.ChangeRecordParams
import kotlinx.coroutines.launch
import javax.inject.Inject

class RecordsViewModel @Inject constructor(
    private val router: Router,
    private val recordsViewDataInteractor: RecordsViewDataInteractor
) : ViewModel() {

    var extra: RecordsExtra? = null

    val records: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            viewModelScope.launch {
                initial.value = listOf(LoaderViewData())
                initial.value = loadRecordsViewData()
            }
            initial
        }
    }

    fun onRecordClick(item: RecordViewData, sharedElements: Map<Any, String>) {
        val preview = ChangeRecordParams.Preview(
            name = item.name,
            timeStarted = item.timeStarted,
            timeFinished = item.timeFinished,
            duration = item.duration,
            iconId = item.iconId,
            color = item.color,
            comment = item.comment
        )

        val params = when (item) {
            is RecordViewData.Tracked -> ChangeRecordParams.Tracked(
                transitionName = TransitionNames.RECORD + item.id,
                id = item.id,
                preview = preview
            )
            is RecordViewData.Untracked -> ChangeRecordParams.Untracked(
                transitionName = TransitionNames.RECORD + item.getUniqueId(),
                timeStarted = item.timeStartedTimestamp,
                timeEnded = item.timeEndedTimestamp,
                preview = preview
            )
        }
        router.navigate(
            screen = Screen.CHANGE_RECORD_FROM_MAIN,
            data = params,
            sharedElements = sharedElements
        )
    }

    fun onVisible() {
        updateRecords()
    }

    fun onNeedUpdate() {
        updateRecords()
    }

    private fun updateRecords() = viewModelScope.launch {
        val data = loadRecordsViewData()
        (records as MutableLiveData).value = data
    }

    private suspend fun loadRecordsViewData(): List<ViewHolderType> {
        return recordsViewDataInteractor.getViewData(extra?.shift.orZero())
    }
}
