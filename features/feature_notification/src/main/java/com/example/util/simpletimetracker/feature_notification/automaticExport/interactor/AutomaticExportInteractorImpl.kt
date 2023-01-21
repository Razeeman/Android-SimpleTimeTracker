package com.example.util.simpletimetracker.feature_notification.automaticExport.interactor

import com.example.util.simpletimetracker.core.extension.post
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.AutomaticExportRepo
import com.example.util.simpletimetracker.domain.interactor.AutomaticExportInteractor
import com.example.util.simpletimetracker.domain.interactor.CsvExportInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.model.DayOfWeek
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.domain.resolver.ResultCode
import com.example.util.simpletimetracker.feature_notification.automaticExport.scheduler.AutomaticExportScheduler
import javax.inject.Inject

class AutomaticExportInteractorImpl @Inject constructor(
    private val scheduler: AutomaticExportScheduler,
    private val csvExportInteractor: CsvExportInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val automaticExportRepo: AutomaticExportRepo,
    private val timeMapper: TimeMapper,
) : AutomaticExportInteractor {

    override fun schedule() {
        val current = System.currentTimeMillis()
        val timestamp = timeMapper
            .getRangeStartAndEnd(
                rangeLength = RangeLength.Day,
                shift = 0,
                firstDayOfWeek = DayOfWeek.MONDAY, // not needed.
                startOfDayShift = 0, // not needed.
            )
            .second
            .let { it - current }
            .takeIf { it > 0 }
            ?: current

        scheduler.schedule(timestamp)
    }

    override fun cancel() {
        scheduler.cancelSchedule()
    }

    override suspend fun export() {
        schedule()
        automaticExportRepo.inProgress.post(true)
        val uri = prefsInteractor.getAutomaticExportUri()
        val result = csvExportInteractor.saveCsvFile(uri, range = null)

        if (result == ResultCode.SUCCESS) {
            schedule()
            prefsInteractor.setAutomaticExportLastSaveTime(System.currentTimeMillis())
        } else {
            cancel()
            prefsInteractor.setAutomaticExportError(true)
            prefsInteractor.setAutomaticExportUri("")
        }
        automaticExportRepo.inProgress.post(false)
    }
}