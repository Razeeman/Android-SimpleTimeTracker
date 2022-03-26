package com.example.util.simpletimetracker.feature_records.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.extension.toParams
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.feature_base_adapter.record.RecordViewData
import com.example.util.simpletimetracker.feature_records.extra.RecordsExtra
import com.example.util.simpletimetracker.feature_records.interactor.RecordsViewDataInteractor
import com.example.util.simpletimetracker.feature_records.model.RecordsState
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
    val calendarData: LiveData<List<RecordViewData.Tracked>> = MutableLiveData()

    fun onRecordClick(item: RecordViewData, sharedElements: Map<Any, String> = emptyMap()) {
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

        val transitionName = when (item) {
            is RecordViewData.Tracked -> TransitionNames.RECORD + item.id
            is RecordViewData.Untracked -> TransitionNames.RECORD + item.getUniqueId()
        }.takeUnless { sharedElements.isEmpty() }.orEmpty()

        val params = when (item) {
            is RecordViewData.Tracked -> ChangeRecordParams.Tracked(
                transitionName = transitionName,
                id = item.id,
                from = ChangeRecordParams.From.Records,
                preview = preview
            )
            is RecordViewData.Untracked -> ChangeRecordParams.Untracked(
                transitionName = transitionName,
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
        when (val state = loadRecordsViewData()) {
            is RecordsState.RecordsData -> records.set(state.data)
            is RecordsState.CalendarData -> calendarData.set(state.data)
        }
    }

    private suspend fun loadRecordsViewData(): RecordsState {
        return recordsViewDataInteractor.getViewData(extra?.shift.orZero())
    }
}
