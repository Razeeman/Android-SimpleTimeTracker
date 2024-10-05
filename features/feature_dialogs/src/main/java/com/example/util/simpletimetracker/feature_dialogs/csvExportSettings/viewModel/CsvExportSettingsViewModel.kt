package com.example.util.simpletimetracker.feature_dialogs.csvExportSettings.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.BaseViewModel
import com.example.util.simpletimetracker.core.extension.lazySuspend
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.extension.toModel
import com.example.util.simpletimetracker.core.extension.toParams
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.feature_base_adapter.recordFilter.FilterViewData
import com.example.util.simpletimetracker.feature_dialogs.csvExportSettings.interactor.CsvExportSettingsViewDataInteractor
import com.example.util.simpletimetracker.feature_dialogs.csvExportSettings.model.CsvExportSettingsFilterType
import com.example.util.simpletimetracker.feature_dialogs.csvExportSettings.viewData.CsvExportSettingsViewData
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.DataExportSettingDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.DataExportSettingsResult
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CsvExportSettingsViewModel @Inject constructor(
    private val router: Router,
    private val prefsInteractor: PrefsInteractor,
    private val timeMapper: TimeMapper,
    private val viewDataInteractor: CsvExportSettingsViewDataInteractor,
) : BaseViewModel() {

    lateinit var extra: DataExportSettingDialogParams

    val viewData: LiveData<CsvExportSettingsViewData> by lazySuspend {
        initialize()
        loadViewData()
    }
    val dataExportSettingsResult: LiveData<DataExportSettingsResult> =
        MutableLiveData()

    private var customFileName: String = ""
    private var rangeLength: RangeLength = RangeLength.All

    fun onNameChange(name: String) {
        viewModelScope.launch {
            if (name != customFileName) {
                customFileName = name
                updateViewData()
            }
        }
    }

    fun onFilterClick(item: FilterViewData) = viewModelScope.launch {
        val itemType = item.type as? CsvExportSettingsFilterType ?: return@launch
        onNewRangeSelected(itemType.rangeLength)
    }

    fun onRangeStartClick() = viewModelScope.launch {
        onRangeClick(
            tag = TIME_STARTED_TAG,
            timestamp = getRange().timeStarted,
        )
    }

    fun onRangeEndClick() = viewModelScope.launch {
        onRangeClick(
            tag = TIME_ENDED_TAG,
            timestamp = getRange().timeEnded,
        )
    }

    fun onDateTimeSet(timestamp: Long, tag: String?) = viewModelScope.launch {
        var (rangeStart, rangeEnd) = getRange()

        when (tag) {
            TIME_STARTED_TAG -> {
                if (timestamp != rangeStart) {
                    rangeStart = timestamp
                    if (timestamp > rangeEnd) rangeEnd = timestamp
                }
            }
            TIME_ENDED_TAG -> {
                if (timestamp != rangeEnd) {
                    rangeEnd = timestamp
                    if (timestamp < rangeStart) rangeStart = timestamp
                }
            }
        }
        onNewRangeSelected(RangeLength.Custom(Range(rangeStart, rangeEnd)))
    }

    fun onExportClick() = viewModelScope.launch {
        DataExportSettingsResult(
            tag = extra.tag,
            customFileName = customFileName,
            range = rangeLength.toParams(),
        ).let(dataExportSettingsResult::set)
    }

    private fun onRangeClick(
        tag: String,
        timestamp: Long,
    ) = viewModelScope.launch {
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()

        router.navigate(
            DateTimeDialogParams(
                tag = tag,
                timestamp = timestamp,
                type = DateTimeDialogType.DATETIME(initialTab = DateTimeDialogType.Tab.DATE),
                useMilitaryTime = useMilitaryTime,
                firstDayOfWeek = firstDayOfWeek,
            ),
        )
    }

    private suspend fun onNewRangeSelected(rangeLength: RangeLength) {
        this.rangeLength = rangeLength
        prefsInteractor.setFileExportRange(rangeLength)
        updateViewData()
    }

    private suspend fun getRange(): Range {
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        val startOfDayShift = prefsInteractor.getStartOfDayShift()

        return if (rangeLength is RangeLength.All) {
            return Range(0, System.currentTimeMillis())
        } else {
            timeMapper.getRangeStartAndEnd(
                rangeLength = rangeLength,
                shift = 0,
                firstDayOfWeek = firstDayOfWeek,
                startOfDayShift = startOfDayShift,
            )
        }
    }

    private fun initialize() {
        rangeLength = extra.selectedRange.toModel()
        customFileName = extra.customFileName
    }

    private suspend fun updateViewData() {
        viewData.set(loadViewData())
    }

    private suspend fun loadViewData(): CsvExportSettingsViewData {
        return viewDataInteractor.getViewData(
            extra = extra,
            customFileName = customFileName,
            rangeLength = rangeLength,
            range = getRange(),
        )
    }

    companion object {
        private const val TIME_STARTED_TAG = "csv_export_settings_time_started_tag"
        private const val TIME_ENDED_TAG = "csv_export_settings_time_ended_tag"
    }
}
