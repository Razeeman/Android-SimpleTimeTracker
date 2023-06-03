package com.example.util.simpletimetracker.domain.resolver

import com.example.util.simpletimetracker.domain.model.Range

interface CsvRepo {

    suspend fun saveCsvFile(
        uriString: String,
        range: Range?,
    ): ResultCode

    suspend fun importCsvFile(
        uriString: String,
    ): ResultCode
}