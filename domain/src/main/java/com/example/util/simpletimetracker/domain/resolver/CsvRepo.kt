package com.example.util.simpletimetracker.domain.resolver

interface CsvRepo {

    enum class ResultCode {
        SUCCESS, ERROR
    }

    suspend fun saveCsvFile(uriString: String): ResultCode
}