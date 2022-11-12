package com.example.util.simpletimetracker.feature_dialogs.csvExportSettings.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.feature_dialogs.csvExportSettings.viewData.CsvExportSettingsViewData
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.DataExportSettingDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.DataExportSettingsResult
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogType
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CsvExportSettingsViewModel @Inject constructor(
    private val router: Router,
    private val prefsInteractor: PrefsInteractor,
    private val timeMapper: TimeMapper,
) : ViewModel() {

    lateinit var extra: DataExportSettingDialogParams

    val viewData: LiveData<CsvExportSettingsViewData> by lazy {
        return@lazy MutableLiveData<CsvExportSettingsViewData>().let { initial ->
            viewModelScope.launch {
                initializeViewData()
                initial.value = loadViewData()
            }
            initial
        }
    }
    val dataExportSettingsResult: LiveData<DataExportSettingsResult> = MutableLiveData()

    private var rangeStart: Long = 0
    private var rangeEnd: Long = 0

    fun onRangeStartClick() {
        viewModelScope.launch {
            val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
            val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()

            router.navigate(
                DateTimeDialogParams(
                    tag = TIME_STARTED_TAG,
                    timestamp = rangeStart,
                    type = DateTimeDialogType.DATETIME(initialTab = DateTimeDialogType.Tab.DATE),
                    useMilitaryTime = useMilitaryTime,
                    firstDayOfWeek = firstDayOfWeek
                )
            )
        }
    }

    fun onRangeEndClick() {
        viewModelScope.launch {
            val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
            val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()

            router.navigate(
                DateTimeDialogParams(
                    tag = TIME_ENDED_TAG,
                    timestamp = rangeEnd,
                    type = DateTimeDialogType.DATETIME(initialTab = DateTimeDialogType.Tab.DATE),
                    useMilitaryTime = useMilitaryTime,
                    firstDayOfWeek = firstDayOfWeek
                )
            )
        }
    }

    fun onDateTimeSet(timestamp: Long, tag: String?) {
        viewModelScope.launch {
            when (tag) {
                TIME_STARTED_TAG -> {
                    if (timestamp != rangeStart) {
                        rangeStart = timestamp
                        if (timestamp > rangeEnd) rangeEnd = timestamp
                        updateViewData()
                    }
                }
                TIME_ENDED_TAG -> {
                    if (timestamp != rangeEnd) {
                        rangeEnd = timestamp
                        if (timestamp < rangeStart) rangeStart = timestamp
                        updateViewData()
                    }
                }
            }
        }
    }

    fun onExportRangeClick() {
        onExport(
            range = DataExportSettingsResult.Range(
                rangeStart = rangeStart,
                rangeEnd = rangeEnd
            )
        )
    }

    fun onExportAllClick() {
        onExport(range = null)
    }

    private fun onExport(range: DataExportSettingsResult.Range?) {
        DataExportSettingsResult(
            tag = extra.tag.orEmpty(),
            range = range
        ).let(dataExportSettingsResult::set)
    }

    private suspend fun updateViewData() {
        viewData.set(loadViewData())
    }

    private fun initializeViewData() {
        rangeEnd = System.currentTimeMillis()
        rangeStart = rangeEnd - TimeUnit.DAYS.toMillis(7)
    }

    private suspend fun loadViewData(): CsvExportSettingsViewData {
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()

        return CsvExportSettingsViewData(
            rangeStartString = rangeStart
                .let { timeMapper.formatDateTime(it, useMilitaryTime) },
            rangeEndString = rangeEnd
                .let { timeMapper.formatDateTime(it, useMilitaryTime) }
        )
    }

    companion object {
        private const val TIME_STARTED_TAG = "csv_export_settings_time_started_tag"
        private const val TIME_ENDED_TAG = "csv_export_settings_time_ended_tag"
    }
}
