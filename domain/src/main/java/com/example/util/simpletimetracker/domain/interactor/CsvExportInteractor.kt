package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.resolver.CsvRepo
import javax.inject.Inject

class CsvExportInteractor @Inject constructor(
    private val csvRepo: CsvRepo,
) {

    suspend fun saveCsvFile(
        uriString: String,
        range: Range?,
    ): CsvRepo.ResultCode {
        return csvRepo.saveCsvFile(
            uriString = uriString,
            range = range,
        )
    }
}