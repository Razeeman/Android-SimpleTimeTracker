package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.resolver.CsvRepo
import com.example.util.simpletimetracker.domain.resolver.ResultCode
import javax.inject.Inject

class CsvExportInteractor @Inject constructor(
    private val csvRepo: CsvRepo,
    private val externalViewsInteractor: UpdateExternalViewsInteractor,
) {

    suspend fun saveCsvFile(uriString: String, range: Range?): ResultCode {
        return csvRepo.saveCsvFile(uriString = uriString, range = range)
    }

    suspend fun importCsvFile(uriString: String): ResultCode {
        val resultCode = csvRepo.importCsvFile(uriString)
        externalViewsInteractor.onCsvImport()
        return resultCode
    }
}