package com.example.util.simpletimetracker.domain.resolver

import com.example.util.simpletimetracker.domain.model.Range

interface IcsRepo {

    suspend fun saveIcsFile(
        uriString: String,
        range: Range?,
    ): ResultCode
}