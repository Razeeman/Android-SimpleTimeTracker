package com.example.util.simpletimetracker.feature_records.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.extension.toParams
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.feature_base_adapter.record.RecordViewData
import com.example.util.simpletimetracker.feature_records.extra.RecordsExtra
import com.example.util.simpletimetracker.feature_records.interactor.RecordsViewDataInteractor
import com.example.util.simpletimetracker.feature_views.TransitionNames
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordFromMainParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordParams
import kotlinx.coroutines.launch
import javax.inject.Inject

class RecordsViewModel @Inject constructor(
    private val router: Router,
    private val recordsViewDataInteractor: RecordsViewDataInteractor
) : ViewModel() {

    var extra: RecordsExtra? = null

    val records: LiveData<List<ViewHolderType>> by lazy {
        MutableLiveData(listOf(LoaderViewData() as ViewHolderType))
    }

    fun onRecordClick(item: RecordViewData, sharedElements: Map<Any, String>) {
        val preview = ChangeRecordParams.Preview(
            name = item.name,
            tagName = item.tagName,
            timeStarted = item.timeStarted,
            timeFinished = item.timeFinished,
            duration = item.duration,
            iconId = item.iconId.toParams(),
            color = item.color,
            comment = item.comment
        )

        val params = when (item) {
            is RecordViewData.Tracked -> ChangeRecordParams.Tracked(
                transitionName = TransitionNames.RECORD + item.id,
                id = item.id,
                from = ChangeRecordParams.From.Records,
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
            data = ChangeRecordFromMainParams(params),
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
