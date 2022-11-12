package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.resolver.IcsRepo
import com.example.util.simpletimetracker.domain.resolver.ResultCode
import javax.inject.Inject

class IcsExportInteractor @Inject constructor(
    private val icsRepo: IcsRepo,
) {

    suspend fun saveIcsFile(uriString: String, range: Range?): ResultCode {
        return icsRepo.saveIcsFile(uriString = uriString, range = range)
    }
}