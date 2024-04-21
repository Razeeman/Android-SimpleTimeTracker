package com.example.util.simpletimetracker.feature_records_all.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.extension.toModel
import com.example.util.simpletimetracker.core.extension.toParams
import com.example.util.simpletimetracker.core.extension.toRecordParams
import com.example.util.simpletimetracker.core.extension.toRunningRecordParams
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.feature_base_adapter.record.RecordViewData
import com.example.util.simpletimetracker.feature_base_adapter.runningRecord.RunningRecordViewData
import com.example.util.simpletimetracker.feature_records_all.interactor.RecordsAllViewDataInteractor
import com.example.util.simpletimetracker.feature_records_all.mapper.RecordsAllViewDataMapper
import com.example.util.simpletimetracker.feature_records_all.model.RecordsAllSortOrder
import com.example.util.simpletimetracker.feature_records_all.viewData.RecordsAllSortOrderViewData
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordFromRecordsAllParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRunningRecordFromRecordsAllParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRunningRecordParams
import com.example.util.simpletimetracker.navigation.params.screen.RecordsAllParams
import com.example.util.simpletimetracker.navigation.params.screen.RecordsFilterParam
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class RecordsAllViewModel @Inject constructor(
    private val router: Router,
    private val recordsAllViewDataInteractor: RecordsAllViewDataInteractor,
    private val recordsAllViewDataMapper: RecordsAllViewDataMapper,
    private val timeMapper: TimeMapper,
    private val prefsInteractor: PrefsInteractor,
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

    fun onRunningRecordClick(
        item: RunningRecordViewData,
        sharedElements: Pair<Any, String>,
    ) = viewModelScope.launch {
        val useMilitaryTimeFormat = prefsInteractor.getUseMilitaryTimeFormat()
        val showSeconds = prefsInteractor.getShowSeconds()

        val params = ChangeRunningRecordParams(
            transitionName = sharedElements.second,
            id = item.id,
            from = ChangeRunningRecordParams.From.Records,
            preview = ChangeRunningRecordParams.Preview(
                name = item.name,
                tagName = item.tagName,
                timeStarted = item.timeStarted,
                timeStartedDateTime = timeMapper.getFormattedDateTime(
                    time = item.timeStartedTimestamp,
                    useMilitaryTime = useMilitaryTimeFormat,
                    showSeconds = showSeconds,
                ).toRunningRecordParams(),
                duration = item.timer,
                durationTotal = item.timerTotal,
                goalTime = item.goalTime.toParams(),
                iconId = item.iconId.toParams(),
                color = item.color,
                comment = item.comment,
            ),
        )
        router.navigate(
            data = ChangeRunningRecordFromRecordsAllParams(params),
            sharedElements = mapOf(sharedElements),
        )
    }

    fun onRecordClick(
        item: RecordViewData,
        sharedElements: Pair<Any, String>,
    ) = viewModelScope.launch {
        val useMilitaryTimeFormat = prefsInteractor.getUseMilitaryTimeFormat()
        val showSeconds = prefsInteractor.getShowSeconds()

        val preview = ChangeRecordParams.Preview(
            name = item.name,
            tagName = item.tagName,
            timeStarted = item.timeStarted,
            timeFinished = item.timeFinished,
            timeStartedDateTime = timeMapper.getFormattedDateTime(
                time = item.timeStartedTimestamp,
                useMilitaryTime = useMilitaryTimeFormat,
                showSeconds = showSeconds,
            ).toRecordParams(),
            timeEndedDateTime = timeMapper.getFormattedDateTime(
                time = item.timeEndedTimestamp,
                useMilitaryTime = useMilitaryTimeFormat,
                showSeconds = useMilitaryTimeFormat,
            ).toRecordParams(),
            duration = item.duration,
            iconId = item.iconId.toParams(),
            color = item.color,
            comment = item.comment,
        )

        val params = when (item) {
            is RecordViewData.Tracked -> ChangeRecordParams.Tracked(
                transitionName = sharedElements.second,
                id = item.id,
                from = ChangeRecordParams.From.RecordsAll,
                preview = preview,
            )
            is RecordViewData.Untracked -> ChangeRecordParams.Untracked(
                transitionName = sharedElements.second,
                timeStarted = item.timeStartedTimestamp,
                timeEnded = item.timeEndedTimestamp,
                preview = preview,
            )
        }
        router.navigate(
            data = ChangeRecordFromRecordsAllParams(params),
            sharedElements = mapOf(sharedElements),
        )
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

    private fun updateRecords() = viewModelScope.launch {
        val data = loadRecordsViewData()
        records.set(data)
    }

    private suspend fun loadRecordsViewData(): List<ViewHolderType> {
        return recordsAllViewDataInteractor.getViewData(
            filter = extra.filter.map(RecordsFilterParam::toModel),
            sortOrder = sortOrder,
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
