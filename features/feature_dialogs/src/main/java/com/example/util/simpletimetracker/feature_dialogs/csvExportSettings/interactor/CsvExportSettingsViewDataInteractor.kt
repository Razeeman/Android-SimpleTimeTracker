package com.example.util.simpletimetracker.feature_dialogs.csvExportSettings.interactor

import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.RangeViewDataMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.model.DayOfWeek
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.recordFilter.FilterViewData
import com.example.util.simpletimetracker.feature_dialogs.R
import com.example.util.simpletimetracker.feature_dialogs.csvExportSettings.model.CsvExportSettingsFilterType
import com.example.util.simpletimetracker.feature_dialogs.csvExportSettings.viewData.CsvExportSettingsViewData
import com.example.util.simpletimetracker.navigation.params.screen.DataExportSettingDialogParams
import javax.inject.Inject

class CsvExportSettingsViewDataInteractor @Inject constructor(
    private val timeMapper: TimeMapper,
    private val colorMapper: ColorMapper,
    private val resourceRepo: ResourceRepo,
    private val prefsInteractor: PrefsInteractor,
    private val rangeViewDataMapper: RangeViewDataMapper,
) {

    suspend fun getViewData(
        extra: DataExportSettingDialogParams,
        customFileName: String,
        rangeLength: RangeLength,
        range: Range,
    ): CsvExportSettingsViewData {
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        val isDarkTheme = prefsInteractor.getDarkMode()
        val fileName = customFileName.ifBlank { extra.defaultFileName }
        val isCustomFileNameSelected = fileName != extra.defaultFileName

        return CsvExportSettingsViewData(
            fileName = fileName,
            fileNameTextColor = mapTextColor(
                isActive = isCustomFileNameSelected,
                isDarkTheme = isDarkTheme,
            ),
            fileNameHint = if (isCustomFileNameSelected) {
                resourceRepo.getString(R.string.change_record_type_name_hint)
            } else {
                resourceRepo.getString(R.string.csv_export_settings_filename_default)
            },
            rangeStartString = timeMapper.formatDateTimeYear(
                time = range.timeStarted,
                useMilitaryTime = useMilitaryTime,
            ),
            rangeEndString = timeMapper.formatDateTimeYear(
                time = range.timeEnded,
                useMilitaryTime = useMilitaryTime,
            ),
            textColor = mapTextColor(
                isActive = rangeLength is RangeLength.Custom,
                isDarkTheme = isDarkTheme,
            ),
            filters = getDateFiltersViewData(
                currentRange = rangeLength,
            ),
        )
    }

    private suspend fun getDateFiltersViewData(
        currentRange: RangeLength,
    ): List<ViewHolderType> {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val startOfDayShift = prefsInteractor.getStartOfDayShift()
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        val lastDays = prefsInteractor.getFileExportLastDays()

        return listOf(
            RangeLength.Day,
            RangeLength.Week,
            RangeLength.Month,
            RangeLength.Year,
            RangeLength.All,
            RangeLength.Last(lastDays),
        ).mapIndexed { index, rangeLength ->
            mapDateRangeFilter(
                rangeLength = rangeLength,
                currentRange = currentRange,
                isDarkTheme = isDarkTheme,
                startOfDayShift = startOfDayShift,
                firstDayOfWeek = firstDayOfWeek,
                index = index,
            )
        }
    }

    private fun mapDateRangeFilter(
        rangeLength: RangeLength,
        currentRange: RangeLength,
        isDarkTheme: Boolean,
        startOfDayShift: Long,
        firstDayOfWeek: DayOfWeek,
        index: Int,
    ): ViewHolderType {
        val selected = currentRange == rangeLength

        return FilterViewData(
            id = index.toLong(),
            type = CsvExportSettingsFilterType(rangeLength),
            name = rangeViewDataMapper.mapToTitle(
                rangeLength = rangeLength,
                position = 0,
                startOfDayShift = startOfDayShift,
                firstDayOfWeek = firstDayOfWeek,
            ),
            color = if (selected) {
                colorMapper.toActiveColor(isDarkTheme)
            } else {
                colorMapper.toInactiveColor(isDarkTheme)
            },
            selected = selected,
            removeBtnVisible = false,
        )
    }

    @ColorInt
    private fun mapTextColor(
        isActive: Boolean,
        isDarkTheme: Boolean,
    ): Int {
        return if (isActive) {
            R.attr.appTextPrimaryColor
        } else {
            R.attr.appTextHintColor
        }.let {
            resourceRepo.getThemedAttr(it, isDarkTheme)
        }
    }
}