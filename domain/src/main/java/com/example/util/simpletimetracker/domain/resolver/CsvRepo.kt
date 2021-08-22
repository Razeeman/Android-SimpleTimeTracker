package com.example.util.simpletimetracker.domain.resolver

import com.example.util.simpletimetracker.domain.model.Range

interface CsvRepo {

    enum class ResultCode {
        SUCCESS, ERROR
    }

    suspend fun saveCsvFile(
        uriString: String,
        range: Range?,
    ): ResultCode
}