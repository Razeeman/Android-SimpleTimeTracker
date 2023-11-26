package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.WidgetType
import com.example.util.simpletimetracker.domain.resolver.CsvRepo
import com.example.util.simpletimetracker.domain.resolver.ResultCode
import javax.inject.Inject

class CsvExportInteractor @Inject constructor(
    private val csvRepo: CsvRepo,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val widgetInteractor: WidgetInteractor,
    private val notificationGoalTimeInteractor: NotificationGoalTimeInteractor,
) {

    suspend fun saveCsvFile(uriString: String, range: Range?): ResultCode {
        return csvRepo.saveCsvFile(uriString = uriString, range = range)
    }

    suspend fun importCsvFile(uriString: String): ResultCode {
        val resultCode = csvRepo.importCsvFile(uriString)

        val runningRecords = runningRecordInteractor.getAll()
        notificationGoalTimeInteractor.checkAndReschedule(runningRecords.map { it.id })
        widgetInteractor.updateWidgets(listOf(WidgetType.STATISTICS_CHART))
        widgetInteractor.updateWidgets(listOf(WidgetType.RECORD_TYPE))

        return resultCode
    }
}