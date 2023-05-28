package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.Record

interface GetUntrackedRecordsInteractor {

    suspend fun get(
        range: Range,
        records: List<Range>,
    ): List<Record>
}